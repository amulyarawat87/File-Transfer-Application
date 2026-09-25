package FileTransferApplication.Controller;

import java.io.IOException;
import java.util.Map;

import FileTransferApplication.Service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import FileTransferApplication.DTO.FileDownloadResponse;
import FileTransferApplication.DTO.UploadResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;

@RestController
@CrossOrigin(origins = "http://localhost:8080/api/v1")
@RequestMapping("api/v1")
public class FileController {

    private final UploadService uploadService;

    public FileController() {
        this.uploadService = new UploadService();
    }

    @GetMapping("/upload")
    public ResponseEntity<?> uploadFile() {
        try{
            UploadResponse response = uploadService.uploadFile();
            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            return ResponseEntity.status(500).body("Upload Service Temporarily Unavailable. Try after some time.");
        }

    }

    @PostMapping("/upload/confirm")
    public ResponseEntity<Map<String, String>> saveFileMetadataToDB(@RequestBody UploadConfirmationRequest request) {
        String shortCode = uploadService.saveFileMetadataToDB(request);
        return ResponseEntity.ok(Map.of("shortCode", shortCode));
    }


    @GetMapping({"/download/{shortCode}", "/s/{shortCode}"})
    public ResponseEntity<FileDownloadResponse> downloadFile(@PathVariable String shortCode) throws IOException {
        FileDownloadResponse response = fileService.downloadService(shortCode);
        if (response == null) {
            // CODE REVIEW [API Design]: Expired and not-found both return 404 — clients can't distinguish TTL expiry from invalid short code. Consider 410 Gone for expired files.
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

}