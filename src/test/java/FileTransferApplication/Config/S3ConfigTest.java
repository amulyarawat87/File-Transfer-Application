package FileTransferApplication.Config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class S3ConfigTest {

    @Test
    void createsS3ClientAndPresignerFromConfiguredProperties() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "access-key");
        ReflectionTestUtils.setField(config, "secretKey", "secret-key");

        S3Client client = config.s3Client();
        S3Presigner presigner = config.s3Presigner();

        assertNotNull(client);
        assertNotNull(presigner);
    }
}