package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
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
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + extension;
        long fileSize = file.getSize();

        Path filePath = Path.of(uploadDir)
                .resolve(fileName);
        Files.createDirectories(filePath.getParent());

        //Logic to Store File in Blob Storage
        s3.uploadFile(file, fileId);

        //Saving to Database
        db.save(fileId, fileName, filePath, fileSize, expiryHours);

        return fileId;
    }
    public ResponseEntity<Resource> downloadService(String id) throws IOException {

        //Retrieving From Database
        FileMetadata file =  db.get(id);


        Path filePath = Path.of(file.getFilePath());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        byte[] s3FileData = s3.downloadFile(id);
        ByteArrayResource resource = new ByteArrayResource(s3FileData);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
}
