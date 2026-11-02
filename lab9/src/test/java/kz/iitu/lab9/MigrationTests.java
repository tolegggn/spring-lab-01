package kz.iitu.lab9;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.DriverManager;
import static org.assertj.core.api.Assertions.*;

class MigrationTests {
    @TempDir Path directory;
    @Test void emptyDatabaseRestartAndIncrementalMigration() throws Exception {
        String url = "jdbc:h2:file:" + directory.resolve("migration-check").toAbsolutePath();
        Flyway initial = Flyway.configure().dataSource(url, "sa", "")
            .locations("classpath:db/migration").target("2").load();
        assertThat(initial.migrate().migrationsExecuted).isEqualTo(2);
        assertThat(initial.info().applied()).extracting(m -> m.getVersion().toString()).containsExactly("1", "2");
        // New Flyway instance + file database simulates restarting the migration lifecycle.
        Flyway restart = Flyway.configure().dataSource(url, "sa", "")
            .locations("classpath:db/migration").target("2").load();
        assertThat(restart.migrate().migrationsExecuted).isZero();
        Flyway upgrade = Flyway.configure().dataSource(url, "sa", "")
            .locations("classpath:db/migration").load();
        assertThat(upgrade.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(upgrade.info().applied()).extracting(m -> m.getVersion().toString()).containsExactly("1", "2", "3");
        try (var connection = DriverManager.getConnection(url, "sa", ""); var stmt = connection.createStatement()) {
            try (var rows = stmt.executeQuery("SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"success\"=TRUE AND \"version\" IS NOT NULL")) {
                rows.next(); assertThat(rows.getInt(1)).isEqualTo(3);
            }
            try (var rows = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME='IDX_ACCOUNTS_OWNER_NAME'")) {
                rows.next(); assertThat(rows.getInt(1)).isEqualTo(1);
            }
            try (var rows = stmt.executeQuery("SELECT COUNT(*) FROM accounts WHERE status='ACTIVE'")) {
                rows.next(); assertThat(rows.getInt(1)).isEqualTo(2);
            }
        }
        System.out.println("EVIDENCE migrations: empty DB applied V1/V2=2; restart=0; upgrade V3=1; successful history rows=3; owner index verified");
    }
}
