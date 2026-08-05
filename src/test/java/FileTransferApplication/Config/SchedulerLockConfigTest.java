package FileTransferApplication.Config;

import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SchedulerLockConfigTest {

    @Test
    void createsLockProviderFromDataSource() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:shedlock;DB_CLOSE_DELAY=-1", "sa", "");

        LockProvider lockProvider = new SchedulerLockConfig().lockProvider(dataSource);

        assertNotNull(lockProvider);
    }
}