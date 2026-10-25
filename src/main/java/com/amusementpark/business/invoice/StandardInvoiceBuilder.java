package com.amusementpark.business.invoice;

import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;

import java.util.ArrayList;
import java.util.List;

/**
 * Default invoice builder: one section per kind of purchase.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {
    private String recipient = "";
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder forRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public InvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        }
        return addSection("Individual tickets", lines);
    }

    @Override
    public InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket groupTicket : groupTickets) {
            lines.add(new InvoiceLine("Group ticket " + groupTicket.getId() + " (" + groupTicket.getDate() + ", "
                    + groupTicket.getTickets().size() + " members)", groupTicket.getPrice()));
        }
        return addSection("Group tickets", lines);
    }

    @Override
    public InvoiceBuilder addSpeedPasses(List<SpeedPass> speedPasses) {
        return addSection("SpeedPasses", passLines(speedPasses));
    }

    @Override
    public InvoiceBuilder addSpecialPasses(List<SpecialPass> specialPasses) {
        return addSection("Special passes", passLines(specialPasses));
    }

    @Override
    public Invoice build() {
        return new Invoice(recipient, sections);
    }

    private static List<InvoiceLine> passLines(List<? extends Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(new InvoiceLine(pass.getId() + (pass.isConsumed() ? " (used)" : ""), pass.getPrice()));
        }
        return lines;
    }

    private InvoiceBuilder addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
        return this;
    }
}
