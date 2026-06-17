package FileTransferApplication.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    private String fileId;

    @Column(unique = true, nullable = false, length = 10)
    private String shortCode;

    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime expiryDateTime;

    @Column(columnDefinition = "TEXT")
    private String encryptionKey;
}