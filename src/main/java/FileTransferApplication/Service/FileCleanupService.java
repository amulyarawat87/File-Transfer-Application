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
        List<FileMetadata> files= fileMetadataRepo.findByExpiryDateTimeBefore(Instant.now());
        // CODE REVIEW [Observability]: No metrics on files deleted/failed — expose cleanup stats via Micrometer/Actuator.
        System.out.println("DEBUG: Running scheduled cleanup task. Total files in DB: " + files.size());

        for(FileMetadata file: files){
            System.out.println("DEBUG: Checking file: " + file.getFileId());
            if(file.getExpiryDateTime().isBefore(Instant.now())) {
                System.out.println("DEBUG: Deleting expired file: " + file.getFileId());
                // CODE REVIEW [Code Quality]: If S3 delete fails, DB row is still removed — causes orphaned S3 objects.
                // Delete S3 first, verify success, then remove DB record; wrap in @Transactional with retry.
                s3.deleteFile(file.getFileId());
                fileMetadataRepo.delete(file);
            }
        }

    }
}
