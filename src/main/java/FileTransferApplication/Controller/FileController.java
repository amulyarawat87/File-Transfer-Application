package FileTransferApplication.Controller;

import FileTransferApplication.DTO.PresignedUrlResponse;
import FileTransferApplication.DTO.UploadConfirmationRequest;
import FileTransferApplication.Service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(exposedHeaders = "Content-Disposition")
@RequestMapping("api")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping({"/download/{id}", "/s/{id}"})
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
    public ResponseEntity<java.util.Map<String, String>> confirmUpload(@RequestBody UploadConfirmationRequest request) {
        String fileId = fileService.confirmUpload(request);
        return ResponseEntity.ok(java.util.Map.of("fileId", fileId));
    }

}