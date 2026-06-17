package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadConfirmationRequest {
    private String fileId;
    private String fileName;
    private long fileSize;
    private String encryptionKey;
}
