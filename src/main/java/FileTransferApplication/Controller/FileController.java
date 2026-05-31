package FileTransferApplication.Controller;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.DTO.FileMetadataResponse;
import FileTransferApplication.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin(exposedHeaders = "Content-Disposition")
@RequestMapping("api")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/upload", method =  RequestMethod.POST)
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Received file: " + file);
        return fileService.uploadService(file);
    }

    @RequestMapping(value = "/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) throws IOException {
        System.out.println("Downloading file Started..." + id);
        return fileService.downloadService(id);
    }

    @GetMapping("/upload/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl() {
        PresignedUrlResponse response = fileService.getPresignedUploadUrl();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/confirm")
    public ResponseEntity<String> confirmUpload(@RequestBody UploadConfirmationRequest request) {
        String fileId = fileService.confirmUpload(request);
        return ResponseEntity.ok("File registered successfully. ID: " + fileId);
    }

    @GetMapping("/download/presigned-url/{id}")
    public ResponseEntity<String> getPresignedDownloadUrl(@PathVariable String id) {
        String presignedUrl = fileService.getPresignedDownloadUrl(id);
        if (presignedUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(presignedUrl);
    }

    @GetMapping("/file-metadata/{id}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable String id) {
        FileMetadataResponse metadata = fileService.getFileMetadata(id);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }

}