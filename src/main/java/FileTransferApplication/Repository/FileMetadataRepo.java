package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepo extends JpaRepository<FileMetadata, String> {
}
