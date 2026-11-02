package kz.iitu.lab9;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final JdbcTemplate jdbc;
    public AuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Transactional(propagation = Propagation.REQUIRED)
    public void required(String message) { write(message); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew(String message) { write(message); }
    private void write(String message) {
        jdbc.update("INSERT INTO audit_records(message) VALUES (?)", message);
    }
}
