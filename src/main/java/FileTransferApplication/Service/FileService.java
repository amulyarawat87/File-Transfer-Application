package FileTransferApplication.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import FileTransferApplication.DTO.FileDownloadResponse;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;

@Service
public class FileService {
    private final FileMetadataDBService db;
    private final S3Service s3;
    private final FileMetadataRepo fileMetadataRepo;

    public FileService(FileMetadataDBService db, S3Service s3, FileMetadataRepo fileMetadataRepo){
        this.db = db;
        this.s3 = s3;
        this.fileMetadataRepo = fileMetadataRepo;
    }


    // CODE REVIEW [Architecture]: Service returns ResponseEntity — HTTP concerns belong in the controller layer;
    // keep FileService returning Resource/byte[] and let the controller build headers/status.
    public FileDownloadResponse downloadService(String shortCode) throws IOException {

        // Lookup by shortCode instead of fileId
        FileMetadata file = fileMetadataRepo.findByShortCode(shortCode)
                .orElse(null);

        if (file == null || file.getExpiryDateTime().isBefore(Instant.now())) {
            // CODE REVIEW [API Design]: Expired and not-found both return 404 — clients can't distinguish TTL expiry
            // from invalid short code. Consider 410 Gone for expired files.
            return null;
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

        // CODE REVIEW [Reliability]: No check that s3FileData.length matches expected fileSize from metadata —
        // corrupted/partial uploads would still be served to the client.
        ByteArrayResource byteArrayResource = new ByteArrayResource(s3FileData);

        String resource = Base64.getEncoder()
                .encodeToString(byteArrayResource.getByteArray());
        // CODE REVIEW [Security]: Unsanitized fileName in Content-Disposition enables header injection
        // (e.g. filename with \r\n). Use ContentDisposition builder or strip/encode special characters.
        return new FileDownloadResponse(
                resource,
                file.getFileName(),
                MediaType.parseMediaType(contentType),
                file.getEncryptionKey()
        );
    }


}