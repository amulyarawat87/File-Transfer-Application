package FileTransferApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileTransferApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileTransferApplication.class);
		app.run(args);
	}

}
