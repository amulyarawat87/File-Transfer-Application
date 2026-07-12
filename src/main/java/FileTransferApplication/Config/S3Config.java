package FileTransferApplication.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    // CODE REVIEW [Security]: Static access-key/secret-key in config — prefer IAM roles (DefaultCredentialsProvider)
    // on EC2/ECS/Lambda so long-lived keys are not embedded in env vars or .env files.
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                // CODE REVIEW [Reliability]: No retry/backoff configuration — transient AWS errors fail immediately.
                // Configure .overrideConfiguration() with retry policy and timeouts.
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        // CODE REVIEW [Maintainability]: Credentials provider logic duplicated for S3Client and S3Presigner —
        // extract a shared StaticCredentialsProvider @Bean to DRY up config.
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}
