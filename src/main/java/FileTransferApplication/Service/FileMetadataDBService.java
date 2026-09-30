package FileTransferApplication.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;

@Service
public class FileMetadataDBService {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;
    
    public void save(FileMetadata fileMetaData) {
        fileMetadataRepo.save(fileMetaData);
    }
    public boolean checkIfShortCodeExists(String shortCode){
        return fileMetadataRepo.existsByShortCode(shortCode);
    }
    public FileMetadata fetchFileDataByShortCode(String shortCode){
        return fileMetadataRepo.findByShortCode(shortCode)
                .orElse(null);
    }
}