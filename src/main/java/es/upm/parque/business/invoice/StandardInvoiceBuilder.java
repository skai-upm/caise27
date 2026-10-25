package es.upm.parque.business.invoice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

/**
 * Builds plain-text invoices with one section per non-empty kind of item.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String header;
    private List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public void reset() {
        header = null;
        sections = new ArrayList<>();
    }

    @Override
    public void buildHeader(String recipientType, String recipient, LocalDate date) {
        header = "  Invoice for " + recipientType + " " + recipient + " - " + date;
    }

    @Override
    public void buildIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        }
        addSection("Individual tickets", lines);
    }

    @Override
    public void buildPasses(List<Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(new InvoiceLine(pass.getKind() + " " + pass.getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        addSection("Personal passes", lines);
    }

    @Override
    public void buildGroupTickets(List<GroupTicket> groupTickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            lines.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.getTickets().size() + " members)", ticket.getPrice()));
        }
        addSection("Group tickets", lines);
    }

    private void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }

    @Override
    public Invoice getResult() {
        if (header == null) {
            throw new IllegalStateException("The invoice header has not been built");
        }
        return new Invoice(header, sections);
    }
}
