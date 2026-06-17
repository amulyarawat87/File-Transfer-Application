package FileTransferApplication;

import FileTransferApplication.Config.DotenvEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileTransferApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileTransferApplication.class);
		// Register the environment listener for dotenv loading
		app.addListeners(new DotenvEnvironmentPostProcessor());
		app.run(args);
	}

}
