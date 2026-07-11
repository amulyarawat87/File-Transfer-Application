package FileTransferApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadConfirmationRequest {
    // CODE REVIEW [Code Quality]: No validation annotations (@NotBlank, @Positive, @Size) —
    // invalid payloads reach service layer unchecked. Add jakarta.validation constraints.
    // CODE REVIEW [API Design]: Client sends encryptionKey in plaintext JSON — document that HTTPS is mandatory
    // and consider never sending keys server-side (client-side-only encryption model).
    private String fileId;
    private String fileName;
    private long fileSize;
    private String encryptionKey;
}
