package FileTransferApplication;

import FileTransferApplication.Config.DotenvEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// CODE REVIEW [Package Structure]: All classes use flat package FileTransferApplication.* — adopt standard
// layered packages (com.example.filetransfer.controller/service/repository) for larger team scalability.
public class FileTransferApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileTransferApplication.class);
		// Register the environment listener for dotenv loading
		app.addListeners(new DotenvEnvironmentPostProcessor());
		// CODE REVIEW [Code Quality]: No global @ControllerAdvice for exception handling — unhandled exceptions
		// return generic 500 HTML/stack traces. Add centralized error handling and consistent JSON error responses.
		app.run(args);
	}

}
