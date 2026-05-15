package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private DBService db;

    @Autowired
    private S3Service s3;


    @Value("${file.upload-dir}")
    private String uploadDir;

    private final byte expiryHours = 3;

    public String uploadService(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf('.'));
        long fileSize = file.getSize();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(expiryHours);

        //Logic to Store File in Blob Storage
        s3.uploadFile(file, fileId);

        //Saving to Database
        db.save(fileId, fileName, fileType, fileSize, expiryTime);

        return fileId;
    }
    public ResponseEntity<Resource> downloadService(String id) throws IOException {

        //Retrieving From Database
        FileMetadata file =  db.get(id);

        // Probe content type from filename instead of just extension
        String contentType = Files.probeContentType(Path.of(file.getFileName()));
        if (contentType == null) contentType = "application/octet-stream";

        System.out.println("DEBUG: Downloading file - ID: " + id + ", FileName: " + file.getFileName() + ", ContentType: " + contentType);

        byte[] s3FileData = s3.downloadFile(id);
        ByteArrayResource resource = new ByteArrayResource(s3FileData);

        System.out.println("DEBUG: Setting header - Content-Disposition: attachment; filename=\"" + file.getFileName() + "\"");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
}
