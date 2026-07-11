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

    // CODE REVIEW [Code Quality]: Hard-coded interval — externalize via @Value for environment-specific tuning.
    private final long schedulerHours = 1;

    // CODE REVIEW [Reliability]: fixedRate runs every hour regardless of previous run duration — overlapping runs
    // possible on slow cleanup. Use fixedDelay or @SchedulerLock (ShedLock) for single-instance guarantee.
    // CODE REVIEW [Scalability]: Runs on every pod in a multi-instance deployment — duplicate S3 deletes and DB writes.
    @Scheduled(fixedRate = schedulerHours * 60 * 60 * 1000)
    public void deleteExpiredFiles(){
        // CODE REVIEW [Optimization]: findAll() loads every row into memory — query only expired records
        // (e.g. findByExpiryDateTimeBefore) and process in batches for large datasets.
        List<FileMetadata> files= fileMetadataRepo.findAll();
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
