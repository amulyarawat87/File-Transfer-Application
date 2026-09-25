package FileTransferApplication.Service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

@Service
public class S3Service {
        private final S3Client s3Client;

        private final S3Presigner s3Presigner;

        public S3Service() {
                this.s3Client = S3Client.builder().build();;
                this.s3Presigner = S3Presigner.builder().build();
        }

    @Value("${aws.bucket-name}")
    private String bucketName;

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
        return presignedRequest.url().toString();
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
    
}