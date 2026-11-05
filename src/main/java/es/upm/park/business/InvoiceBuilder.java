package es.upm.park.business;

import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.Pass;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder abstraction for invoices. The construction steps have empty default
 * implementations; each concrete builder implements only the steps that apply to its
 * recipient type. Sections without lines are never added.
 */
public abstract class InvoiceBuilder {

    private String recipient;
    private LocalDate date;
    private final List<Invoice.Section> sections = new ArrayList<>();

    /** Starts a new invoice for the given recipient and issue date. */
    public void reset(String recipient, LocalDate date) {
        this.recipient = recipient;
        this.date = date;
        sections.clear();
    }

    /** Step: individual tickets bought directly by the recipient. */
    public void buildIndividualTickets(List<IndividualTicket> tickets) {
        // not applicable by default
    }

    /** Step: group-ticket purchases of the recipient. */
    public void buildGroupTickets(List<GroupTicket> groupTickets) {
        // not applicable by default
    }

    /** Step: personal passes of the recipient. */
    public void buildPasses(List<Pass> passes) {
        // not applicable by default
    }

    /** Adds a section unless it has no lines. */
    protected void addSection(String title, List<Invoice.Line> lines) {
        if (!lines.isEmpty()) {
            sections.add(new Invoice.Section(title, lines));
        }
    }

    public Invoice getResult() {
        return new Invoice(recipient, date, sections);
    }
}
