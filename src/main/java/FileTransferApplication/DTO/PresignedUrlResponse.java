package FileTransferApplication.DTO;

public class PresignedUrlResponse {
    private String fileId;
    private String presignedUrl;
    private long expiresIn; // seconds

    public PresignedUrlResponse(String fileId, String presignedUrl, long expiresIn) {
        this.fileId = fileId;
        this.presignedUrl = presignedUrl;
        this.expiresIn = expiresIn;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
