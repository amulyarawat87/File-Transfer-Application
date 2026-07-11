package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepo extends JpaRepository<FileMetadata, String> {
    Optional<FileMetadata> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    // CODE REVIEW [Optimization]: Add findByExpiryDateTimeBefore(LocalDateTime) so FileCleanupService
    // doesn't need findAll() and filter in application code.
    // CODE REVIEW [Performance]: Consider @Index on shortCode and expiryDateTime columns for lookup/cleanup queries.
}