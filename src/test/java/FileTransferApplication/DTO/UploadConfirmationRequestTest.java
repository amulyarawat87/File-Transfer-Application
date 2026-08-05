package FileTransferApplication.DTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UploadConfirmationRequestTest {

    @Test
    void recordExposesAllComponents() {
        UploadConfirmationRequest request = new UploadConfirmationRequest("file-1", "report.txt", 42L, "key-json");

        assertEquals("file-1", request.fileId());
        assertEquals("report.txt", request.fileName());
        assertEquals(42L, request.fileSize());
        assertEquals("key-json", request.encryptionKey());
        assertEquals("UploadConfirmationRequest[fileId=file-1, fileName=report.txt, fileSize=42, encryptionKey=key-json]", request.toString());
    }
}