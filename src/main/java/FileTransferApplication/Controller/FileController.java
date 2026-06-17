package FileTransferApplication.Controller;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin(exposedHeaders = "Content-Disposition")
@RequestMapping("api")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping({"/download/{shortCode}", "/s/{shortCode}"})
    public ResponseEntity<Resource> downloadFile(@PathVariable String shortCode) throws IOException {
        return fileService.downloadService(shortCode);
    }

    @GetMapping("/upload/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl() {
        PresignedUrlResponse response = fileService.getPresignedUploadUrl();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/confirm")
    public ResponseEntity<Map<String, String>> confirmUpload(@RequestBody UploadConfirmationRequest request) {
        String shortCode = fileService.confirmUpload(request);
        return ResponseEntity.ok(Map.of("shortCode", shortCode)); // changed from fileId to shortCode
    }
}