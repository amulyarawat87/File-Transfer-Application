package FileTransferApplication.DTO;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
/**
 * @param fileId CODE REVIEW [Security]: fileId is returned to client before upload is confirmed — ensure it cannot be reused to hijack another user's upload by validating ownership on confirmUpload.
 */
public record FileDownloadResponse(Resource resource, String fileName, MediaType contentType) {
}