package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class DBService {
    
    // CODE REVIEW [Code Quality]: Thin wrapper around repository — consider merging into FileService or using repo directly.
    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    // CODE REVIEW [Security]: encryptionKey stored as plaintext in DB — encrypt at rest (KMS/envelope encryption)
    // or store only a key reference; never persist raw key material if avoidable.
    // CODE REVIEW [Maintainability]: 7 positional parameters — replace with a FileMetadata builder or record
    // to reduce argument-order bugs when fields are added.
    public void save(String fileId, String fileName, String fileType, long fileSize, LocalDateTime expiryTime, String encryptionKey, String shortCode) {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setFileId(fileId);
        fileMetadata.setShortCode(shortCode);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileType(fileType);
        fileMetadata.setFileSize(fileSize);
        fileMetadata.setExpiryDateTime(expiryTime);
        fileMetadata.setEncryptionKey(encryptionKey);

        fileMetadataRepo.save(fileMetadata);

        // CODE REVIEW [Code Quality]: Use SLF4J logger; avoid logging fileId/shortCode at INFO in production.
        // CODE REVIEW [Observability]: No audit trail — log who uploaded, when, and file size for compliance/debugging.
        System.out.println("File Saved Successfully - ShortCode: " + shortCode + ", FileId: " + fileId);
    }

    // CODE REVIEW [Code Quality]: Generic RuntimeException leaks no HTTP-friendly error; use a custom NotFoundException
    // handled by @ControllerAdvice. Also appears unused — verify or remove.
    public FileMetadata get(String id) {
        return fileMetadataRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }
}