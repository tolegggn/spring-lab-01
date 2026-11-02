package kz.iitu.lab9;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PropagationService {
    private final AuditService audit;
    private final AccountRepository accounts;
    public PropagationService(AuditService audit, AccountRepository accounts) {
        this.audit = audit;
        this.accounts = accounts;
    }
    @Transactional
    public void outer(boolean independentAudit) {
        accounts.findById(1L).orElseThrow().debit(new BigDecimal("10.00"));
        accounts.flush();
        if (independentAudit) audit.requiresNew("REQUIRES_NEW audit");
        else audit.required("REQUIRED audit");
        throw new IllegalStateException("Outer transaction failed");
    }
}
