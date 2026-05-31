package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class DBService {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    public void save(String fileId, String fileName, String fileType, long fileSize, LocalDateTime expiryTime){
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setFileId(fileId);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileType(fileType);
        fileMetadata.setFileSize(fileSize);
        fileMetadata.setExpiryDateTime(expiryTime);
        fileMetadataRepo.save(fileMetadata);

        System.out.println("File Saved Successfully");
    }

    // Overloaded save method with encryption key (for presigned URL flow)
    public void save(String fileId, String fileName, String fileType, long fileSize, LocalDateTime expiryTime, String encryptionKey){
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setFileId(fileId);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileType(fileType);
        fileMetadata.setFileSize(fileSize);
        fileMetadata.setExpiryDateTime(expiryTime);
        fileMetadata.setEncryptionKey(encryptionKey);
        fileMetadataRepo.save(fileMetadata);

        System.out.println("File Saved Successfully with Encryption Key");
    }

    public FileMetadata get(String id){
        return fileMetadataRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

}
