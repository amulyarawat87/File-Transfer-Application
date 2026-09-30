package FileTransferApplication.Service;

import java.time.Duration;

import FileTransferApplication.Config.S3Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

@Service
public class S3Service {
        private final S3Client s3Client;
        private final S3Presigner s3Presigner;

        @Value("${aws.bucket-name}")
        private String bucketName;

        public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
            this.s3Client = s3Client;
            this.s3Presigner = s3Presigner;
        }



    // Generate presigned PUT (upload) URL
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
    public String generatePresignedGetUrl(String key, Duration duration) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
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