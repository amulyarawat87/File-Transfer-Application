package FileTransferApplication.DTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PresignedUrlResponseTest {

    @Test
    void recordExposesAllComponents() {
        PresignedUrlResponse response = new PresignedUrlResponse("file-1", "https://example.com/upload", 60L);

        assertEquals("file-1", response.fileId());
        assertEquals("https://example.com/upload", response.presignedUrl());
        assertEquals(60L, response.expiresInSeconds());
        assertEquals("PresignedUrlResponse[fileId=file-1, presignedUrl=https://example.com/upload, expiresInSeconds=60]", response.toString());
    }
}