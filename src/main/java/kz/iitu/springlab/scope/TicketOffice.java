package kz.iitu.springlab.scope;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class TicketOffice {

    private final ObjectProvider<Ticket> provider;

    public TicketOffice(ObjectProvider<Ticket> provider) {
        this.provider = provider;
    }

    public String twoTickets() {
        Ticket a = provider.getObject();
        Ticket b = provider.getObject();

        return "ticket1=" + a.id() + ", ticket2=" + b.id();
    }
}