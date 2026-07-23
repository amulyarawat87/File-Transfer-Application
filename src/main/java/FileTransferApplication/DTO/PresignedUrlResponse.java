package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;

/**
 * @param fileId CODE REVIEW [Security]: fileId is returned to client before upload is confirmed — ensure it cannot be reused to hijack another user's upload by validating ownership on confirmUpload.
 */
public record PresignedUrlResponse(String fileId, String presignedUrl, long expiresInSeconds) {
}
