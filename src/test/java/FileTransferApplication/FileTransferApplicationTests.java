package FileTransferApplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:filetransfer;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"aws.region=us-east-1",
		"aws.bucket-name=test-bucket",
		"aws.access-key=test-access-key",
		"aws.secret-key=test-secret-key",
		"scheduler.fixed-rate=60000"
})
class FileTransferApplicationTests {

	@Test
	void contextLoads() {
	}

}
