package FileTransferApplication.Service;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
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

    private final long expiryHours = 24; // Files expire after 24 hours
    private final long urlExpiryDurationMinutes = 10;

    public ResponseEntity<Resource> downloadService(String id) throws IOException {

        //Retrieving From Database
        FileMetadata file =  db.get(id);

        if(file == null || file.getExpiryDateTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.notFound().build();
        }

        // Probe content type from filename instead of just extension
        String contentType = Files.probeContentType(Path.of(file.getFileName()));
        if (contentType == null) contentType = "application/octet-stream";

        System.out.println("DEBUG: Downloading file - ID: " + id + ", FileName: " + file.getFileName() + ", ContentType: " + contentType);

        byte[] s3FileData = s3.downloadFile(id);

        // Decrypt with the per-file key when the presigned upload flow stored one.
        byte[] decryptedFile = (file.getEncryptionKey() != null && !file.getEncryptionKey().isBlank())
            ? secureFile.decryptFile(s3FileData, file.getEncryptionKey())
            : secureFile.decryptFile(s3FileData);

        ByteArrayResource resource = new ByteArrayResource(decryptedFile);

        System.out.println("DEBUG: Setting header - Content-Disposition: attachment; filename=\"" + file.getFileName() + "\"");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }

    // Generate presigned URL for uploading directly to S3 (Client encrypts before upload)
    public PresignedUrlResponse getPresignedUploadUrl() {
        String fileId = UUID.randomUUID().toString();
        Duration urlExpiry = Duration.ofMinutes(urlExpiryDurationMinutes);
        String presignedUrl = s3.generatePresignedPutUrl(fileId, urlExpiry);
        return new PresignedUrlResponse(fileId, presignedUrl, urlExpiry.getSeconds());
    }

    // Confirm file upload completion (after client uploads via presigned URL)
    public String confirmUpload(UploadConfirmationRequest request) {
        String fileType = request.getFileName().substring(request.getFileName().lastIndexOf('.'));
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(expiryHours);
        
        db.save(request.getFileId(), request.getFileName(), fileType, request.getFileSize(), expiryTime, request.getEncryptionKey());
        
        return request.getFileId();
    }
}
