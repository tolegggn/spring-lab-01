package kz.iitu.lab9;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Version
    private Long version;
    @Column(nullable = false, length = 20)
    private String status;

    protected Account() {}
    public Long getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public Long getVersion() { return version; }
    public String getStatus() { return status; }

    static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount must be positive with at most two decimal places");
        }
    }
    public void debit(BigDecimal amount) {
        validateAmount(amount);
        if (balance.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");
        balance = balance.subtract(amount);
    }
    public void credit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.add(amount);
    }
}
