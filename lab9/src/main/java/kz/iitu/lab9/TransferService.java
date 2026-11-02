package kz.iitu.lab9;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class TransferService {
    private final AccountRepository accounts;
    public TransferService(AccountRepository accounts) { this.accounts = accounts; }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        perform(fromId, toId, amount, false);
    }

    // Separate public entry point ensures the call crosses Spring's transaction proxy.
    @Transactional
    public void transferWithFailure(Long fromId, Long toId, BigDecimal amount) {
        perform(fromId, toId, amount, true);
    }

    private void perform(Long fromId, Long toId, BigDecimal amount, boolean fail) {
        Account.validateAmount(amount);
        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new IllegalArgumentException("Two distinct account IDs are required");
        }
        Account from = accounts.findById(fromId).orElseThrow(() -> new NoSuchElementException("Source account not found"));
        Account to = accounts.findById(toId).orElseThrow(() -> new NoSuchElementException("Destination account not found"));
        from.debit(amount);
        if (fail) {
            accounts.flush(); // Send the debit to the database before proving rollback.
            throw new IllegalStateException("Simulated failure after debit");
        }
        to.credit(amount); // Managed entities are persisted by dirty checking at commit.
    }
}
