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
// CODE REVIEW [Security]: @CrossOrigin with no origins allows any site to call this API.
// Restrict to known frontend origins (e.g. origins = "https://your-app.com") in production.
@CrossOrigin(exposedHeaders = "Content-Disposition")
// CODE REVIEW [API Design]: Missing leading slash and version prefix — use "/api/v1" for clearer routing and future versioning.
@RequestMapping("api")
public class FileController {

    // CODE REVIEW [Code Quality]: Prefer constructor injection over field @Autowired.
    // Constructor injection makes dependencies explicit, eases testing, and avoids null beans.
    @Autowired
    private FileService fileService;

    @GetMapping({"/download/{shortCode}", "/s/{shortCode}"})
    // CODE REVIEW [Security]: No validation/sanitization on shortCode — add @Pattern or length limits
    // to block malformed input and reduce brute-force enumeration of short codes.
    // CODE REVIEW [Security]: No auth or rate limiting on download; anyone with a short code can fetch files.
    // CODE REVIEW [Error Handling]: Declares throws IOException but service may throw unchecked S3/crypto exceptions —
    // these won't map to a proper HTTP status without @ControllerAdvice.
    public ResponseEntity<Resource> downloadFile(@PathVariable String shortCode) throws IOException {
        return fileService.downloadService(shortCode);
    }

    // CODE REVIEW [Security]: Presigned URL endpoint is unauthenticated — attackers can exhaust S3 quota
    // or spam metadata records. Add rate limiting and/or API key validation.
    // CODE REVIEW [API Design]: GET with no query params — consider accepting fileName/contentType upfront
    // so presigned URL can enforce upload constraints server-side.
    @GetMapping("/upload/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl() {
        PresignedUrlResponse response = fileService.getPresignedUploadUrl();
        return ResponseEntity.ok(response);
    }

    // CODE REVIEW [Code Quality]: Missing @Valid on request body — invalid/null fields reach the service layer.
    // CODE REVIEW [Security]: No verification that the caller actually uploaded to S3 before confirming.
    // CODE REVIEW [API Design]: Returns 200 with Map — use a typed response DTO and return 201 Created for new resources.
    // CODE REVIEW [Reliability]: No idempotency key — duplicate POST /confirm with same fileId can create multiple short codes.
    @PostMapping("/upload/confirm")
    public ResponseEntity<Map<String, String>> confirmUpload(@RequestBody UploadConfirmationRequest request) {
        String shortCode = fileService.confirmUpload(request);
        return ResponseEntity.ok(Map.of("shortCode", shortCode)); // changed from fileId to shortCode
    }
}