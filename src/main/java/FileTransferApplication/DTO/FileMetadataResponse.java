package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataResponse {
    private String fileId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String encryptionKey;
}
