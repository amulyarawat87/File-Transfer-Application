package FileTransferApplication.Service;

import FileTransferApplication.DTO.UploadResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Utils.FileIDGenerator;
import FileTransferApplication.Utils.ShortCodeGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class UploadService {
    private final Duration MAX_PRESIGNED_URL_EXPIRY_DURATION;
    private final S3Service s3Service;
    private final FileMetadataDBService fileMetadataDBService;
    private static short MAX_RETRIES = 3;

    public UploadService(){
        this.MAX_PRESIGNED_URL_EXPIRY_DURATION = Duration.ofSeconds(60);
        this.s3Service = new S3Service();
        this.fileMetadataDBService = new FileMetadataDBService();
    }
    public UploadResponse uploadFile(){
        String fileId = FileIDGenerator.generateFileId();
        String presignedUrl = s3Service.generatePresignedPutUrl(fileId, MAX_PRESIGNED_URL_EXPIRY_DURATION);
        return new UploadResponse(fileId, presignedUrl);
    }
    public String saveFileMetadataToDB(UploadConfirmationRequest request) {
        String generatedShortCode = generateShortCode();
        FileMetadata fileMetaData = FileMetadata.builder()
                                    .id(request.fileId())
                                    .fileName(request.fileName())
                                    .fileType(request.fileName().substring(request.fileName().lastIndexOf('.')))
                                    .fileSize(request.fileSize())
                                    .createdAt(Instant.now())
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
