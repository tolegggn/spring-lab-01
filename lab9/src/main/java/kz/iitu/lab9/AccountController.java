package kz.iitu.lab9;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountRepository accounts;
    private final TransferService transfers;
    public AccountController(AccountRepository accounts, TransferService transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }
    @GetMapping("/accounts")
    public List<Account> accounts() { return accounts.findAll(); }
    public record TransferRequest(Long fromId, Long toId, BigDecimal amount) {}
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestBody TransferRequest request) {
        transfers.transfer(request.fromId(), request.toId(), request.amount());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(IllegalArgumentException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(NoSuchElementException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler({IllegalStateException.class, OptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(Exception e) { return Map.of("error", "Transfer conflict: refresh account data before retrying"); }
}
