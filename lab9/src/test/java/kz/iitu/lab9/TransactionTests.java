package kz.iitu.lab9;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1")
class TransactionTests {
    @Autowired TransferService transfers;
    @Autowired PropagationService propagation;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired EntityManagerFactory emf;

    @BeforeEach
    void reset() {
        jdbc.update("UPDATE accounts SET balance = CASE WHEN id=1 THEN 1000.00 ELSE 500.00 END, version=0");
        jdbc.update("DELETE FROM audit_records");
    }
    BigDecimal balance() { return balance(1); }
    BigDecimal balance(int id) { return jdbc.queryForObject("SELECT balance FROM accounts WHERE id=?", BigDecimal.class, id); }
    void balances(String alice, String bob) {
        assertThat(balance(1)).isEqualByComparingTo(alice);
        assertThat(balance(2)).isEqualByComparingTo(bob);
    }
    @Test void successfulTransfer() {
        transfers.transfer(1L, 2L, new BigDecimal("100.00"));
        balances("900.00", "600.00");
        System.out.println("EVIDENCE transfer: Alice=900.00, Bob=600.00");
    }
    @Test void insufficientFundsRollBack() {
        assertThatThrownBy(() -> transfers.transfer(1L, 2L, new BigDecimal("1001.00")))
            .isInstanceOf(IllegalStateException.class).hasMessage("Insufficient funds");
        balances("1000.00", "500.00");
        System.out.println("EVIDENCE insufficient funds: unchanged balances 1000.00 / 500.00");
    }
    @Test void failureAfterFlushedDebitRollsBack() {
        assertThatThrownBy(() -> transfers.transferWithFailure(1L, 2L, new BigDecimal("100.00")))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("after debit");
        balances("1000.00", "500.00");
        System.out.println("EVIDENCE unchecked exception after SQL debit: rollback to 1000.00 / 500.00");
    }
    @Test void invalidRequestsLeaveBalancesUnchanged() {
        for (String value : new String[]{"0", "-1", "0.001"}) {
            assertThatThrownBy(() -> transfers.transfer(1L, 2L, new BigDecimal(value)))
                .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> transfers.transfer(1L, 1L, BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> transfers.transfer(1L, 99L, BigDecimal.ONE)).isInstanceOf(java.util.NoSuchElementException.class);
        balances("1000.00", "500.00");
    }
    @Test void requiredAuditRollsBackWithOuter() {
        assertThatThrownBy(() -> propagation.outer(false)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_records", Integer.class)).isZero();
        balances("1000.00", "500.00");
        System.out.println("EVIDENCE REQUIRED: outer debit rolled back, audit count=0");
    }
    @Test void requiresNewAuditSurvivesOuterRollback() {
        assertThatThrownBy(() -> propagation.outer(true)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT message FROM audit_records", String.class)).isEqualTo("REQUIRES_NEW audit");
        balances("1000.00", "500.00");
        System.out.println("EVIDENCE REQUIRES_NEW: outer debit rolled back, committed audit count=1");
    }
    void isolation(int isolation, String expectedSecondRead) {
        var manager = new DataSourceTransactionManager(dataSource);
        var reader = new TransactionTemplate(manager);
        reader.setIsolationLevel(isolation);
        reader.setTimeout(10);
        var writer = new TransactionTemplate(manager);
        writer.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        writer.setTimeout(10);
        // REQUIRES_NEW suspends the reader and uses another physical connection.
        // JDBC reads avoid JPA's first-level cache masking database isolation.
        reader.executeWithoutResult(status -> {
            assertThat(balance()).isEqualByComparingTo("1000.00");
            writer.executeWithoutResult(inner -> jdbc.update("UPDATE accounts SET balance=900.00, version=version+1 WHERE id=1"));
            assertThat(balance()).isEqualByComparingTo(expectedSecondRead);
        });
        assertThat(balance()).isEqualByComparingTo("900.00");
        System.out.println("EVIDENCE isolation=" + isolation + ": first=1000.00, second=" + expectedSecondRead + ", after reader commit=900.00");
    }
    @Test void readCommittedSeesCommittedChange() {
        isolation(TransactionDefinition.ISOLATION_READ_COMMITTED, "900.00");
    }
    @Test void repeatableReadRetainsSnapshot() {
        isolation(TransactionDefinition.ISOLATION_REPEATABLE_READ, "1000.00");
    }
    @Test void optimisticLockRejectsStaleUpdate() {
        EntityManager first = emf.createEntityManager();
        EntityManager stale = emf.createEntityManager();
        try {
            first.getTransaction().begin();
            stale.getTransaction().begin();
            Account a = first.find(Account.class, 1L);
            Account b = stale.find(Account.class, 1L);
            assertThat(a.getVersion()).isEqualTo(b.getVersion());
            a.credit(new BigDecimal("10.00"));
            first.getTransaction().commit();
            b.credit(new BigDecimal("20.00"));
            assertThatThrownBy(() -> stale.getTransaction().commit())
                .isInstanceOf(RollbackException.class).hasCauseInstanceOf(OptimisticLockException.class);
            assertThat(balance()).isEqualByComparingTo("1010.00");
            assertThat(jdbc.queryForObject("SELECT version FROM accounts WHERE id=1", Long.class)).isEqualTo(1);
            System.out.println("EVIDENCE optimistic lock: stale commit rejected, balance=1010.00, version=1");
        } finally {
            if (first.getTransaction().isActive()) first.getTransaction().rollback();
            if (stale.getTransaction().isActive()) stale.getTransaction().rollback();
            first.close(); stale.close();
        }
    }
}
