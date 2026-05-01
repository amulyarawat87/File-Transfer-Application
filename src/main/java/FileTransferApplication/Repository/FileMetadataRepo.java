package FileTransferApplication.Repository;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FileMetadataRepo extends JpaRepository<FileMetadata, String> {
}
