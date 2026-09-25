package FileTransferApplication.DTO;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
/**
 * @param resource
 */
public record FileDownloadResponse(String resource, String fileName, MediaType contentType, String encryptionKey) {
}