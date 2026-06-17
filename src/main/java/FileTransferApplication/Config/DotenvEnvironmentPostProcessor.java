package FileTransferApplication.Config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Determine which .env file to load based on active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        String envFile = ".env.dev"; // default
        
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
        
        Map<String, Object> properties = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            properties.put(entry.getKey(), entry.getValue());
        });
        
        MapPropertySource propertySource = new MapPropertySource("dotenv", properties);
        environment.getPropertySources().addFirst(propertySource);
    }
}
