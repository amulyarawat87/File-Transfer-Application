package FileTransferApplication.DTO;

/**
 * @param fileId
 */

public record UploadConfirmationRequest(String fileId, String fileName, long fileSize, String encryptionKey) {
}
