package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepo extends JpaRepository<FileMetadata, String> {
    Optional<FileMetadata> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}