package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DBServiceTest {

    @Mock
    private FileMetadataRepo fileMetadataRepo;

    @InjectMocks
    private DBService dbService;

    @Test
    void saveMapsArgumentsIntoEntity() {
        Instant expiry = Instant.parse("2026-08-05T12:00:00Z");

        dbService.save("file-1", "archive.txt", ".txt", 42L, expiry, "key-json", "ABC123");

        ArgumentCaptor<FileMetadata> captor = ArgumentCaptor.forClass(FileMetadata.class);
        verify(fileMetadataRepo).save(captor.capture());

        FileMetadata saved = captor.getValue();
        assertEquals("file-1", saved.getFileId());
        assertEquals("archive.txt", saved.getFileName());
        assertEquals(".txt", saved.getFileType());
        assertEquals(42L, saved.getFileSize());
        assertEquals(expiry, saved.getExpiryDateTime());
        assertEquals("key-json", saved.getEncryptionKey());
        assertEquals("ABC123", saved.getShortCode());
    }

    @Test
    void getReturnsStoredMetadata() {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId("file-1");

        when(fileMetadataRepo.findById("file-1")).thenReturn(Optional.of(metadata));

        assertEquals(metadata, dbService.get("file-1"));
    }

    @Test
    void getThrowsWhenMissing() {
        when(fileMetadataRepo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> dbService.get("missing"));
    }
}