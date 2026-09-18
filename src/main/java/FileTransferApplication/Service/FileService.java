package FileTransferApplication.Service;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileService {
    private final DBService db;
    private final S3Service s3;
    private final SecurityService secureFile;
    private final FileMetadataRepo fileMetadataRepo;

    public FileService(DBService db, S3Service s3, SecurityService secureFile, FileMetadataRepo fileMetadataRepo){
        this.db = db;
        this.s3 = s3;
        this.secureFile = secureFile;
        this.fileMetadataRepo = fileMetadataRepo;
    }


    // CODE REVIEW [Code Quality]: Hard-coded magic numbers — externalize via @Value("${file.expiry-hours:24}")
    // so ops can tune TTL without redeploying.
    private final long expiryHours = 24;
    private final long urlExpiryDurationSeconds = 60;

    // CODE REVIEW [Architecture]: Service returns ResponseEntity — HTTP concerns belong in the controller layer;
    // keep FileService returning Resource/byte[] and let the controller build headers/status.
    public ResponseEntity<Resource> downloadService(String shortCode) throws IOException {

        // Lookup by shortCode instead of fileId
        FileMetadata file = fileMetadataRepo.findByShortCode(shortCode)
                .orElse(null);

        if (file == null || file.getExpiryDateTime().isBefore(Instant.now())) {
            // CODE REVIEW [API Design]: Expired and not-found both return 404 — clients can't distinguish TTL expiry
            // from invalid short code. Consider 410 Gone for expired files.
            return ResponseEntity.notFound().build();
        }

        // CODE REVIEW [Code Quality]: probeContentType uses filename only, not file bytes — unreliable MIME detection.
        String contentType = Files.probeContentType(Path.of(file.getFileName()));
        if (contentType == null) contentType = "application/octet-stream";

        // CODE REVIEW [Code Quality]: Use SLF4J logger instead of System.out.println; avoid logging filenames in production.
        // CODE REVIEW [Observability]: No structured logging, metrics, or tracing — add Micrometer counters for
        // download success/failure and OpenTelemetry spans around S3/decrypt calls for production debugging.
        System.out.println("DEBUG: Downloading file - ShortCode: " + shortCode + ", FileName: " + file.getFileName() + ", ContentType: " + contentType);

        // CODE REVIEW [Optimization]: Entire file is loaded into heap (S3 → byte[] → decrypt → ByteArrayResource).
        // Large files will cause OOM; stream from S3 and decrypt in chunks, or use presigned GET redirects.
        // Download from S3 using internal fileId
        byte[] s3FileData = s3.downloadFile(file.getFileId());

        byte[] decryptedFile = (file.getEncryptionKey() != null && !file.getEncryptionKey().isBlank())
                ? secureFile.decryptFile(s3FileData, file.getEncryptionKey())
                : secureFile.decryptFile(s3FileData);

        // CODE REVIEW [Reliability]: No check that s3FileData.length matches expected fileSize from metadata —
        // corrupted/partial uploads would still be served to the client.
        ByteArrayResource resource = new ByteArrayResource(decryptedFile);

        // CODE REVIEW [Security]: Unsanitized fileName in Content-Disposition enables header injection
        // (e.g. filename with \r\n). Use ContentDisposition builder or strip/encode special characters.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }

    public PresignedUrlResponse getPresignedUploadUrl() {
        String fileId = UUID.randomUUID().toString();
        Duration urlExpiry = Duration.ofSeconds(urlExpiryDurationSeconds);
        String presignedUrl = s3.generatePresignedPutUrl(fileId, urlExpiry);
        // CODE REVIEW [Data Integrity]: fileId is generated but no pending-upload record is created —
        // orphaned S3 objects accumulate if client never calls /confirm.
        return new PresignedUrlResponse(fileId, presignedUrl, urlExpiry.getSeconds());
    }

    public String confirmUpload(UploadConfirmationRequest request) {
        // CODE REVIEW [Code Quality]: No null/blank checks on request fields — NPE if fileName or fileId is missing.
        // CODE REVIEW [Code Quality]: lastIndexOf('.') returns -1 for extensionless names → StringIndexOutOfBoundsException.
        String fileType = request.fileName().substring(request.fileName().lastIndexOf('.'));
        Instant expiryTime = Instant.now().plusSeconds(expiryHours * 3600);
        // CODE REVIEW [Concurrency]: Race between existsByShortCode check and save — two threads could get the same code.
        // Use DB unique constraint + retry, or generate codes inside a transaction with SELECT FOR UPDATE.
        String shortCode = generateUniqueShortCode();

        // CODE REVIEW [Security]: confirmUpload trusts client-supplied fileId/fileSize without verifying the S3 object exists
        // or that Content-Length matches — allows phantom DB records for never-uploaded files.
        // CODE REVIEW [Security]: Duplicate fileId is not rejected; re-confirming could overwrite or throw on unique constraint.
        // CODE REVIEW [Maintainability]: confirmUpload lacks @Transactional — partial failure leaves inconsistent S3/DB state.
        db.save(request.fileId(), request.fileName(), fileType, request.fileSize(), expiryTime, request.encryptionKey(), shortCode);

        return shortCode; // return shortCode instead of fileId
    }

    // CODE REVIEW [Optimization]: Unbounded retry loop on collision — add max attempts and fail fast.
    // CODE REVIEW [Security]: 6-char short codes (~56B combos) are brute-forceable without rate limiting on /download.
    private String generateUniqueShortCode() {
        String code;
        do {
            code = ShortCodeGenerator.generate();
        } while (fileMetadataRepo.existsByShortCode(code));
        return code;
    }
}