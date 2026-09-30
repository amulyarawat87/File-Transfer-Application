package FileTransferApplication.DTO;

import org.springframework.http.MediaType;
/**
 * @param presignedGetUrl
 */
public record DownloadResponse(String fileName, String presignedGetUrl, MediaType contentType, String encryptionKey) {
}