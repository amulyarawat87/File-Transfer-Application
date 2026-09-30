package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class FileCleanupService {

    
    private final FileMetadataRepo fileMetadataRepo;
    private final S3Service s3;

    public FileCleanupService(FileMetadataRepo fileMetadataRepo, S3Service s3) {
        this.fileMetadataRepo = fileMetadataRepo;
        this.s3 = s3;
    }

    @Scheduled(fixedDelayString = "${scheduler.fixed-rate}")
    @SchedulerLock(name = "deleteExpiredFiles", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void deleteExpiredFiles(){
        List<FileMetadata> files= fileMetadataRepo.findByExpiryTimeBefore(Instant.now());
        System.out.println("DEBUG: Running scheduled cleanup task. Total files in DB: " + files.size());

        for(FileMetadata file: files) {
            if (file.getExpiryTime().isBefore(Instant.now())) {
                s3.deleteFile(file.getFileId());
                fileMetadataRepo.delete(file);
            }
        }

    }
}
