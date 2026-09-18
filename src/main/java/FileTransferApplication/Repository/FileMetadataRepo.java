package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FileMetadataRepo extends JpaRepository<FileMetadata, String> {
    Optional<FileMetadata> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    List<FileMetadata> findByExpiryDateTimeBefore(Instant dateTime);
    // CODE REVIEW [Performance]: Consider @Index on shortCode and expiryDateTime columns for lookup/cleanup queries.
}