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

    private final long schedulerHours = 1;

    @Scheduled(fixedRate = schedulerHours * 60 * 60 * 1000)
    public void deleteExpiredFiles(){
        List<FileMetadata> files= fileMetadataRepo.findAll();
        System.out.println("DEBUG: Running scheduled cleanup task. Total files in DB: " + files.size());

        for(FileMetadata file: files){
            System.out.println("DEBUG: Checking file: " + file.getFileId());
            if(file.getExpiryDateTime().isBefore(LocalDateTime.now())) {
                System.out.println("DEBUG: Deleting expired file: " + file.getFileId());
                s3.deleteFile(file.getFileId());
                fileMetadataRepo.delete(file);
            }
        }

    }
}
