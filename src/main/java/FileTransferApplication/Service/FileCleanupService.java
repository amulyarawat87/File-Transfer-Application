package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileCleanupService {

    
    private final FileMetadataRepo fileMetadataRepo;
    private final S3Service s3;

    public FileCleanupService(FileMetadataRepo fileMetadataRepo, S3Service s3) {
        this.fileMetadataRepo = fileMetadataRepo;
        this.s3 = s3;
    }

    // CODE REVIEW [Reliability]: Use @SchedulerLock (ShedLock) for single-instance guarantee.
    // CODE REVIEW [Scalability]: Runs on every pod in a multi-instance deployment — duplicate S3 deletes and DB writes.
    @Scheduled(fixedDelayString = "${scheduler.fixed-rate}")
    public void deleteExpiredFiles(){
        List<FileMetadata> files= fileMetadataRepo.findByExpiryDateTimeBefore(LocalDateTime.now());
        // CODE REVIEW [Code Quality]: Replace System.out.println with SLF4J logger at DEBUG level.
        // CODE REVIEW [Observability]: No metrics on files deleted/failed — expose cleanup stats via Micrometer/Actuator.
        System.out.println("DEBUG: Running scheduled cleanup task. Total files in DB: " + files.size());

        for(FileMetadata file: files){
            System.out.println("DEBUG: Checking file: " + file.getFileId());
            // CODE REVIEW [Reliability]: LocalDateTime.now() uses JVM default timezone — use Instant/UTC consistently
            // across servers in different regions to avoid premature or delayed expiry checks.
            if(file.getExpiryDateTime().isBefore(LocalDateTime.now())) {
                System.out.println("DEBUG: Deleting expired file: " + file.getFileId());
                // CODE REVIEW [Code Quality]: If S3 delete fails, DB row is still removed — causes orphaned S3 objects.
                // Delete S3 first, verify success, then remove DB record; wrap in @Transactional with retry.
                s3.deleteFile(file.getFileId());
                fileMetadataRepo.delete(file);
            }
        }

    }
}
