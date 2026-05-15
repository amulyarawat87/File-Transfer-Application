package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileCleanupService {

    @Autowired
    private FileMetadataRepo fileMetadataRepo;

    @Autowired
    private S3Service s3;

    private final long schedulerHours = 1; // Run cleanup every hour

    @Scheduled(fixedRate = schedulerHours * 60 * 60 * 1000) // Convert hours to milliseconds
    public void deleteExpiredFiles(){
        List<FileMetadata> files= fileMetadataRepo.findAll();

        for(FileMetadata file: files){
            System.out.println("File cleaned up Starting");
            if(file.getExpiryDateTime().isBefore(LocalDateTime.now())) {
                System.out.println("File Removed" + file.toString());
                s3.deleteFile(file.getFileId());
                fileMetadataRepo.delete(file);
            }
        }

    }
}
