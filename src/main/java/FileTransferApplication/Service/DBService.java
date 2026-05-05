package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class DBService {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    public void save(String fileId, String fileName, Path filePath, long fileSize, byte expiryHours){
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setFileId(fileId);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileSize(fileSize);
        fileMetadata.setFilePath(filePath.toString());
        fileMetadata.setExpiryDate(LocalDateTime.now().plusHours(expiryHours));
        fileMetadataRepo.save(fileMetadata);

        System.out.println("File Saved Successfully");
    }

    public FileMetadata get(String id){
        return fileMetadataRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

}
