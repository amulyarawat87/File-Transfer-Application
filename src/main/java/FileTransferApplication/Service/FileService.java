package FileTransferApplication.Service;

import FileTransferApplication.Model.FileMetadata;
import FileTransferApplication.Repository.FileMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private FileMetadataRepo fileMetadataRepo;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final int expiryHours = 3;

    public String uploadService(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + extension;

        Path filePath = Path.of(uploadDir)
                .resolve(fileName);
        Files.createDirectories(filePath.getParent());

        //Logic to Store File in Blob Storage
        Files.copy(file.getInputStream(), filePath ,  StandardCopyOption.REPLACE_EXISTING);

        // Store to DB
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setFileId(fileId);
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileSize(file.getSize());
        fileMetadata.setFilePath(filePath.toString());
        fileMetadata.setExpiryDate(LocalDateTime.now().plusHours(expiryHours));
        fileMetadataRepo.save(fileMetadata);

        return fileId;
    }
    public ResponseEntity<Resource> downloadService(String id) throws IOException {

        FileMetadata file =  fileMetadataRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));

        Path filePath = Path.of(file.getFilePath());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
}
