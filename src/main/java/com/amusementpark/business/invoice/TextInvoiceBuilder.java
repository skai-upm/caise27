package com.amusementpark.business.invoice;

import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder of text invoices: each step adds one section with its item lines and updates the total.
 */
public class TextInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private LocalDate date;
    private final List<Invoice.Section> sections = new ArrayList<>();
    private BigDecimal total = new BigDecimal("0.00");

    @Override
    public InvoiceBuilder start(String recipient, LocalDate date) {
        this.recipient = recipient;
        this.date = date;
        return this;
    }

    @Override
    public InvoiceBuilder addTickets(List<IndividualTicket> tickets) {
        List<Invoice.Line> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(new Invoice.Line("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()));
        }
        return addSection("Tickets", lines);
    }

    @Override
    public InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets) {
        List<Invoice.Line> lines = new ArrayList<>();
        for (GroupTicket gt : groupTickets) {
            lines.add(new Invoice.Line("Group ticket " + gt.getId() + " (" + gt.getDate() + ", "
                    + gt.getTickets().size() + " members)", gt.getPrice()));
        }
        return addSection("Group tickets", lines);
    }

    @Override
    public InvoiceBuilder addPasses(List<Pass> passes) {
        List<Invoice.Line> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(new Invoice.Line(p.getKind() + " " + p.getId() + (p.isConsumed() ? " (used)" : ""),
                    p.getPrice()));
        }
        return addSection("Passes", lines);
    }

    @Override
    public Invoice build() {
        if (recipient == null) {
            throw new IllegalStateException("start() must be called before build()");
        }
        return new Invoice(recipient, date, sections, total);
    }

    private InvoiceBuilder addSection(String title, List<Invoice.Line> lines) {
        if (!lines.isEmpty()) {
            sections.add(new Invoice.Section(title, lines));
            for (Invoice.Line line : lines) {
                total = total.add(line.amount());
            }
        }
        return this;
    }
}
