package FileTransferApplication.Model;


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
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime expiryDateTime;
}
