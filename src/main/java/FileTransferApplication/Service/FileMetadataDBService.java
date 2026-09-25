package FileTransferApplication.Service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;

@Service
public class FileMetadataDBService {
    
    // CODE REVIEW [Code Quality]: Thin wrapper around repository — consider merging into FileService or using repo directly.
    @Autowired
    private FileMetadataRepo fileMetadataRepo;
    public void save(FileMetadata fileMetaData) {
        fileMetadataRepo.save(fileMetaData);
    }
    public boolean checkIfShortCodeExists(String shortCode){
        return fileMetadataRepo.existsByShortCode(shortCode);
    }
}