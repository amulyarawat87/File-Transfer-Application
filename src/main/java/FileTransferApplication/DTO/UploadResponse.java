package FileTransferApplication.DTO;


/**
 * @param presignedPutUrl
 */
public record UploadResponse(String fileId, String presignedPutUrl) {
}
