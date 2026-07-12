package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedUrlResponse {
    // CODE REVIEW [Security]: fileId is returned to client before upload is confirmed — ensure it cannot be
    // reused to hijack another user's upload by validating ownership on confirmUpload.
    private String fileId;
    private String presignedUrl;
    // CODE REVIEW [API Design]: expiresIn is in seconds but field name is ambiguous — rename to expiresInSeconds
    // or return ISO-8601 expiry timestamp for client clock-skew tolerance.
    private long expiresIn;
}
