package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;

/**
 * @param fileId CODE REVIEW [Code Quality]: No validation annotations (@NotBlank, @Positive, @Size) — invalid payloads reach service layer unchecked. Add jakarta.validation constraints.
 *               CODE REVIEW [API Design]: Client sends encryptionKey in plaintext JSON — document that HTTPS is mandatory and consider never sending keys server-side (client-side-only encryption model).
 */

public record UploadConfirmationRequest(String fileId, String fileName, long fileSize, String encryptionKey) {
}
