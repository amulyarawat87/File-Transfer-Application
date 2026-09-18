package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:repo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "aws.region=us-east-1",
    "aws.access-key=test-access-key",
    "aws.secret-key=test-secret-key",
    "aws.bucket-name=test-bucket",
    "scheduler.fixed-rate=60000"
})
class FileMetadataRepoTest {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    @Test
    void saveAndFindByShortCodeWork() {
        FileMetadata metadata = fileMetadata("file-1", "ABC123", Instant.now().plusSeconds(60));
        fileMetadataRepo.save(metadata);

        assertTrue(fileMetadataRepo.existsByShortCode("ABC123"));
        assertEquals("file-1", fileMetadataRepo.findByShortCode("ABC123").orElseThrow().getFileId());
    }

    @Test
    void findByExpiryDateTimeBeforeReturnsOnlyExpiredRows() {
        Instant now = Instant.now();
        FileMetadata expired = fileMetadata("file-1", "ABC123", now.minusSeconds(60));
        FileMetadata active = fileMetadata("file-2", "XYZ789", now.plusSeconds(60));

        fileMetadataRepo.saveAll(List.of(expired, active));

        List<FileMetadata> results = fileMetadataRepo.findByExpiryDateTimeBefore(now);

        assertEquals(1, results.size());
        assertEquals("file-1", results.get(0).getFileId());
    }

    private static FileMetadata fileMetadata(String fileId, String shortCode, Instant expiry) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId(fileId);
        metadata.setShortCode(shortCode);
        metadata.setFileName("report.txt");
        metadata.setFileType(".txt");
        metadata.setFileSize(42L);
        metadata.setExpiryDateTime(expiry);
        metadata.setEncryptionKey("key-json");
        return metadata;
    }
}