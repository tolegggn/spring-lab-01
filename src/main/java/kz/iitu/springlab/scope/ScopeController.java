package kz.iitu.springlab.scope;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScopeController {

    private final TicketOffice office;

    public ScopeController(TicketOffice office) {
        this.office = office;
    }

    @GetMapping("/scope")
    public String scope() {
        return office.twoTickets();
    }
}