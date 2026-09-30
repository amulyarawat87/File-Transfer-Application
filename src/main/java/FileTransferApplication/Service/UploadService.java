package FileTransferApplication.Service;

import FileTransferApplication.DTO.UploadResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Utils.FileIDGenerator;
import FileTransferApplication.Utils.ShortCodeGenerator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UploadService {
    private final S3Service s3Service;
    private final FileMetadataDBService fileMetadataDBService;
    private static final Duration MAX_PRESIGNED_PUT_URL_EXPIRY_DURATION = Duration.ofSeconds(60);
    private static final int EXPIRY_HOURS = 24;
    private static final short MAX_RETRIES = 3;

    public UploadService(S3Service s3Service, FileMetadataDBService fileMetadataDBService){
        this.s3Service = s3Service;
        this.fileMetadataDBService = fileMetadataDBService;
    }
    public UploadResponse uploadFile(){
        String fileId = FileIDGenerator.generateFileId();
        String presignedPutUrl = s3Service.generatePresignedPutUrl(fileId, MAX_PRESIGNED_PUT_URL_EXPIRY_DURATION);
        return new UploadResponse(fileId, presignedPutUrl);
    }
    public String saveFileMetadataToDB(UploadConfirmationRequest request) {
        String generatedShortCode = generateShortCode();
        Instant fileExpiryTime = Instant.now().plus(EXPIRY_HOURS, ChronoUnit.HOURS);
        FileMetadata fileMetaData = FileMetadata.builder()
                                    .fileId(request.fileId())
                                    .fileName(request.fileName())
                                    .fileType(request.fileName().substring(request.fileName().lastIndexOf('.')))
                                    .fileSize(request.fileSize())
                                    .expiryTime(fileExpiryTime)
                                    .encryptionKey(request.encryptionKey())
                                    .shortCode(generatedShortCode)
                                    .build();

        fileMetadataDBService.save(fileMetaData);

        return generatedShortCode;
    }

    private String generateShortCode() {
        int retryCount = 0;
        while(retryCount<=MAX_RETRIES) {
            String currentGeneratedShortCode = ShortCodeGenerator.generateShortCode();
            if(!fileMetadataDBService.checkIfShortCodeExists(currentGeneratedShortCode)) return currentGeneratedShortCode;
            retryCount++;
        }
        return null;
    }
}
