package FileTransferApplication.Service;

import FileTransferApplication.DTO.DownloadResponse;
import FileTransferApplication.Model.FileMetadata;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@Service
public class DownloadService {

    private final S3Service s3Service;
    private final FileMetadataDBService fileMetadataDBService;
    private static final Duration MAX_PRESIGNED_GET_URL_EXPIRY_DURATION = Duration.ofSeconds(60);

    public DownloadService(S3Service s3Service, FileMetadataDBService fileMetadataDBService) {
        this.s3Service = s3Service;
        this.fileMetadataDBService = fileMetadataDBService;
    }

    public DownloadResponse downloadFile(String shortCode) throws IOException {

        FileMetadata file = fileMetadataDBService.fetchFileDataByShortCode(shortCode);

        if(file==null){
            return null;
        }

        Instant currTime = Instant.now();
        if(currTime.isAfter(file.getExpiryTime())){
            return null;
        }

        String contentType = Files.probeContentType(Path.of(file.getFileName()));
        if (contentType == null) contentType = "application/octet-stream";

        String presignedGetUrl = s3Service.generatePresignedGetUrl(file.getFileId(), MAX_PRESIGNED_GET_URL_EXPIRY_DURATION);

        return new DownloadResponse(
                file.getFileName(),
                presignedGetUrl,
                MediaType.parseMediaType(contentType),
                file.getEncryptionKey()
        );
    }
}
