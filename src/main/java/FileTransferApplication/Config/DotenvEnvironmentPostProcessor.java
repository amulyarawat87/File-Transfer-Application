package FileTransferApplication.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    // CODE REVIEW [DevOps]: Not registered via META-INF/spring.factories — only works because main() adds it manually.
    // Register properly so it also loads during tests and alternate entry points.
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Determine which .env file to load based on active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        String envFile = ".env.dev"; // default
        
        // CODE REVIEW [Code Quality]: Only inspects activeProfiles[0] — ignores additional profiles in multi-profile setups.
        if (activeProfiles.length > 0) {
            String profile = activeProfiles[0];
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                envFile = ".env.prod";
            } else if ("dev".equalsIgnoreCase(profile) || "development".equalsIgnoreCase(profile)) {
                envFile = ".env.dev";
            }
        }
        
        Dotenv dotenv = Dotenv.configure()
                .filename(envFile)
                .ignoreIfMissing()
                .load();
        
        // CODE REVIEW [Reliability]: No validation that required vars (DB_URL, AWS keys) are present after load —
        // app starts with null credentials and fails at runtime instead of at boot.
        Map<String, Object> properties = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            properties.put(entry.getKey(), entry.getValue());
        });
        
        MapPropertySource propertySource = new MapPropertySource("dotenv", properties);
        // CODE REVIEW [Security]: addFirst overrides system env vars — dotenv values take precedence over OS-level secrets.
        // Consider addLast or explicit precedence rules so production secrets aren't accidentally shadowed.
        environment.getPropertySources().addFirst(propertySource);
    }
}
