package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileCleanupServiceTest {

    @Mock
    private FileMetadataRepo fileMetadataRepo;

    @Mock
    private S3Service s3Service;

    @Test
    void deleteExpiredFilesDoesNothingWhenRepositoryReturnsNoFiles() {
        when(fileMetadataRepo.findByExpiryDateTimeBefore(any())).thenReturn(List.of());

        new FileCleanupService(fileMetadataRepo, s3Service).deleteExpiredFiles();

        verify(s3Service, never()).deleteFile(any());
        verify(fileMetadataRepo, never()).delete(any());
    }

    @Test
    void deleteExpiredFilesDeletesExpiredEntriesAfterS3Removal() {
        FileMetadata expired = fileMetadata("file-1", Instant.now().minusSeconds(60));
        FileMetadata future = fileMetadata("file-2", Instant.now().plusSeconds(60));

        when(fileMetadataRepo.findByExpiryDateTimeBefore(any())).thenReturn(List.of(expired, future));

        new FileCleanupService(fileMetadataRepo, s3Service).deleteExpiredFiles();

        InOrder inOrder = inOrder(s3Service, fileMetadataRepo);
        inOrder.verify(s3Service).deleteFile("file-1");
        inOrder.verify(fileMetadataRepo).delete(expired);
        verify(s3Service, never()).deleteFile("file-2");
        verify(fileMetadataRepo, never()).delete(future);
    }

    private static FileMetadata fileMetadata(String fileId, Instant expiry) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileId(fileId);
        fileMetadata.setExpiryDateTime(expiry);
        return fileMetadata;
    }
}