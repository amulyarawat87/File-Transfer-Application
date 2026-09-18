package FileTransferApplication.Model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileMetadataTest {

    @Test
    void gettersSettersAndAllArgsConstructorWork() {
        Instant expiry = Instant.parse("2026-08-05T12:00:00Z");
        FileMetadata metadata = new FileMetadata("file-1", "ABC123", "report.txt", ".txt", 42L, expiry, "key-json");

        assertEquals("file-1", metadata.getFileId());
        assertEquals("ABC123", metadata.getShortCode());
        assertEquals("report.txt", metadata.getFileName());
        assertEquals(".txt", metadata.getFileType());
        assertEquals(42L, metadata.getFileSize());
        assertEquals(expiry, metadata.getExpiryDateTime());
        assertEquals("key-json", metadata.getEncryptionKey());

        metadata.setShortCode("XYZ789");
        assertEquals("XYZ789", metadata.getShortCode());
    }

    @Test
    void equalsAndHashCodeCompareEntityState() {
        Instant expiry = Instant.parse("2026-08-05T12:00:00Z");
        FileMetadata first = new FileMetadata("file-1", "ABC123", "report.txt", ".txt", 42L, expiry, "key-json");
        FileMetadata second = new FileMetadata("file-1", "ABC123", "report.txt", ".txt", 42L, expiry, "key-json");
        FileMetadata different = new FileMetadata("file-2", "ABC123", "report.txt", ".txt", 42L, expiry, "key-json");

        assertEquals(first, second);
        assertNotEquals(first, different);
    }
}