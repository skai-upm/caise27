package es.upm.parque.business.invoice;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Default invoice builder: one section per kind of item, total = sum of all lines.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder start(String recipient) {
        this.recipient = recipient;
        sections.clear();
        return this;
    }

    @Override
    public InvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets) {
        addSection("Individual tickets", tickets.stream()
                .map(ticket -> new InvoiceLine("Ticket " + ticket.getId() + " for " + ticket.getDate(), ticket.getPrice()))
                .toList());
        return this;
    }

    @Override
    public InvoiceBuilder addPasses(List<Pass> passes) {
        addSection("Personal passes", passes.stream()
                .map(pass -> new InvoiceLine(pass.getKind().getDisplayName() + " " + pass.getId()
                        + " (ticket " + pass.getTicket().getId() + (pass.isConsumed() ? ", consumed)" : ")"),
                        pass.getPrice()))
                .toList());
        return this;
    }

    @Override
    public InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets) {
        addSection("Group tickets", groupTickets.stream()
                .map(ticket -> new InvoiceLine("Group ticket " + ticket.getId() + " for " + ticket.getDate()
                        + " (" + ticket.getTickets().size() + " members)", ticket.getPrice()))
                .toList());
        return this;
    }

    @Override
    public Invoice build() {
        BigDecimal total = sections.stream()
                .flatMap(section -> section.lines().stream())
                .map(InvoiceLine::amount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        return new Invoice(recipient, sections, total);
    }

    private void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }
}
