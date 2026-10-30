package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.invoice.TextInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Director of the invoice builder: decides which sections apply to each recipient.
 */
public class InvoiceService {

    private final TicketService ticketService;

    public InvoiceService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Directly bought individual tickets of the user; tickets inside group tickets are not counted. */
    public Invoice userInvoice(User user) {
        List<InvoiceLine> tickets = new ArrayList<>();
        for (IndividualTicket t : ticketService.getTickets()) {
            if (t.getUser() == user) {
                tickets.add(new InvoiceLine("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()));
            }
        }
        InvoiceBuilder builder = new TextInvoiceBuilder();
        return builder.forRecipient("USER", user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")")
                .addSection("Individual tickets", tickets)
                .build();
    }

    /** Group tickets bought by the group, without their constituent tickets. */
    public Invoice groupInvoice(Group group) {
        List<InvoiceLine> groupTickets = new ArrayList<>();
        for (GroupTicket gt : ticketService.getGroupTickets()) {
            if (gt.getGroup() == group) {
                groupTickets.add(new InvoiceLine("Group ticket " + gt.getId() + " (" + gt.getDate() + ", "
                        + gt.getTickets().size() + " members)", gt.getPrice()));
            }
        }
        InvoiceBuilder builder = new TextInvoiceBuilder();
        return builder.forRecipient("GROUP", group.getName() + " (" + group.getId() + ")")
                .addSection("Group tickets", groupTickets)
                .build();
    }
}
