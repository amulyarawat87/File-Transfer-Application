package FileTransferApplication.Service;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private DBService db;

    @Autowired
    private S3Service s3;

    @Autowired
    private SecurityService secureFile;

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    private final long expiryHours = 24;
    private final long urlExpiryDurationMinutes = 10;

    public ResponseEntity<Resource> downloadService(String shortCode) throws IOException {

        // Lookup by shortCode instead of fileId
        FileMetadata file = fileMetadataRepo.findByShortCode(shortCode)
                .orElse(null);

        if (file == null || file.getExpiryDateTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(Path.of(file.getFileName()));
        if (contentType == null) contentType = "application/octet-stream";

        System.out.println("DEBUG: Downloading file - ShortCode: " + shortCode + ", FileName: " + file.getFileName() + ", ContentType: " + contentType);

        // Download from S3 using internal fileId
        byte[] s3FileData = s3.downloadFile(file.getFileId());

        byte[] decryptedFile = (file.getEncryptionKey() != null && !file.getEncryptionKey().isBlank())
                ? secureFile.decryptFile(s3FileData, file.getEncryptionKey())
                : secureFile.decryptFile(s3FileData);

        ByteArrayResource resource = new ByteArrayResource(decryptedFile);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }

    public PresignedUrlResponse getPresignedUploadUrl() {
        String fileId = UUID.randomUUID().toString();
        Duration urlExpiry = Duration.ofMinutes(urlExpiryDurationMinutes);
        String presignedUrl = s3.generatePresignedPutUrl(fileId, urlExpiry);
        return new PresignedUrlResponse(fileId, presignedUrl, urlExpiry.getSeconds());
    }

    public String confirmUpload(UploadConfirmationRequest request) {
        String fileType = request.getFileName().substring(request.getFileName().lastIndexOf('.'));
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(expiryHours);
        String shortCode = generateUniqueShortCode();

        db.save(request.getFileId(), request.getFileName(), fileType, request.getFileSize(), expiryTime, request.getEncryptionKey(), shortCode);

        return shortCode; // return shortCode instead of fileId
    }

    private String generateUniqueShortCode() {
        String code;
        do {
            code = ShortCodeGenerator.generate();
        } while (fileMetadataRepo.existsByShortCode(code));
        return code;
    }
}