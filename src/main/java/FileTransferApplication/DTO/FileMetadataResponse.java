package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// CODE REVIEW [Maintainability]: DTO appears unused in codebase — remove dead code or wire it into a GET /metadata endpoint.
public class FileMetadataResponse {
    private String fileId;
    private String fileName;
    private String fileType;
    private long fileSize;
    // CODE REVIEW [Security]: Exposing encryptionKey in API responses leaks secrets to clients/logs.
    // Remove from response DTO or return a masked/redacted value only when strictly needed.
    private String encryptionKey;
}
