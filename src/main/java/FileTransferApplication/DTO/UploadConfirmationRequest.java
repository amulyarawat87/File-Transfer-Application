package FileTransferApplication.DTO;

public class UploadConfirmationRequest {
    private String fileId;
    private String fileName;
    private long fileSize;
    private String encryptionKey;  // Client-generated encryption key (JSON format)

    public UploadConfirmationRequest() {
    }

    public UploadConfirmationRequest(String fileId, String fileName, long fileSize, String encryptionKey) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.encryptionKey = encryptionKey;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
