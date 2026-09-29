package FileTransferApplication.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import FileTransferApplication.Service.DownloadService;
import FileTransferApplication.Service.UploadService;

import FileTransferApplication.DTO.UploadResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.DTO.DownloadResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000/")
@RequestMapping("api/v1")
public class FileController {

    private final UploadService uploadService;
    private final DownloadService downloadService;

    public FileController(UploadService uploadService, DownloadService downloadService) {
        this.uploadService = uploadService;
        this.downloadService = downloadService;
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
        public ResponseEntity<?> downloadFile(@PathVariable String shortCode) {
            try{
                DownloadResponse response = downloadService.downloadFile(shortCode);
                return ResponseEntity.ok(response);
            }
            catch (Exception e){
                return ResponseEntity.status(500).body("Upload Service Temporarily Unavailable. Try after some time.");
            }

        }
    }