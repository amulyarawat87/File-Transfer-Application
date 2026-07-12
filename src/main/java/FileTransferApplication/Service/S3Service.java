package FileTransferApplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class S3Service {

    // CODE REVIEW [Code Quality]: Prefer constructor injection over field @Autowired.
    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${aws.bucket-name}")
    private String bucketName;

    // CODE REVIEW [Reliability]: S3Client is synchronous — under load, blocking I/O ties up servlet threads.
    // Consider async client or offloading to a thread pool for high-throughput scenarios.
    // CODE REVIEW [Code Quality]: Method always returns true — return void or propagate S3 exceptions instead.
    public boolean uploadFile(byte[] file, String key, String contentType) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType)
                        .build(),
                RequestBody.fromBytes(file)
        );

        return true;
    }

    // Download
    // CODE REVIEW [Code Quality]: No error handling — missing S3 keys throw unhandled SdkException to the caller.
    // CODE REVIEW [Optimization]: getObjectAsBytes loads the full object into memory; use streaming for large files.
    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );

        return response.asByteArray();
    }
    // Delete
    // CODE REVIEW [Code Quality]: Swallows no errors but also doesn't verify deletion succeeded or log failures.
    // CODE REVIEW [Reliability]: S3 deleteObject is idempotent but silent — no check for NoSuchKey vs actual failures.
    public void deleteFile(String key){
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }

    // Generate presigned PUT (upload) URL
    // CODE REVIEW [Security]: No content-type or max-size constraint on presigned PUT — clients can upload
    // arbitrary content types/sizes. Add conditions (Content-Type, content-length-range) to the presign request.
    public String generatePresignedPutUrl(String key, Duration duration) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        // CODE REVIEW [Maintainability]: S3Presigner and S3Client are never closed — register @PreDestroy shutdown hooks
        // to avoid resource leaks on hot redeploys.
        return presignedRequest.url().toString();
    }

    // Generate presigned GET (download) URL
    // CODE REVIEW [Code Quality]: Dead code — method is never called; remove or use for download redirect optimization.
    public String generatePresignedGetUrl(String key, Duration duration) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}