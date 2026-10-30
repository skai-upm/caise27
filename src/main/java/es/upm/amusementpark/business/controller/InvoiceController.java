package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Director of the invoice builder: decides which sections apply to each recipient.
 */
public class InvoiceController {

    private TicketController ticketController;
    private Supplier<InvoiceBuilder> builderFactory;

    private static InvoiceController instance;

    private InvoiceController() {
    }

    /** Returns the unique instance of this controller. */
    public static synchronized InvoiceController getInstance() {
        if (instance == null) {
            instance = new InvoiceController();
        }
        return instance;
    }

    /** Injects the ticket controller and the factory of builders used for each invoice. */
    public void configure(TicketController ticketController, Supplier<InvoiceBuilder> builderFactory) {
        this.ticketController = ticketController;
        this.builderFactory = builderFactory;
    }

    /**
     * Directly bought individual tickets of the user (tickets inside group tickets are not counted again) and the
     * personal passes attached to any of the user's individual tickets, including those inside group tickets.
     */
    public Invoice userInvoice(User user) {
        List<InvoiceLine> tickets = new ArrayList<>();
        for (IndividualTicket t : ticketController.getTickets()) {
            if (t.getUser() == user) {
                tickets.add(new InvoiceLine("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()));
            }
        }
        List<InvoiceLine> passes = new ArrayList<>();
        for (IndividualTicket t : ticketController.getAllIndividualTickets()) {
            if (t.getUser() == user) {
                for (Pass pass : t.getPasses()) {
                    passes.add(new InvoiceLine(pass.getKind() + " " + pass.getId() + " (ticket " + t.getId() + ")",
                            pass.getPrice()));
                }
            }
        }
        InvoiceBuilder builder = builderFactory.get();
        return builder.forRecipient("USER", user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")")
                .addSection("Individual tickets", tickets)
                .addSection("Personal passes", passes)
                .build();
    }

    /** Group tickets bought by the group, without their constituent tickets. */
    public Invoice groupInvoice(Group group) {
        List<InvoiceLine> groupTickets = new ArrayList<>();
        for (GroupTicket gt : ticketController.getGroupTickets()) {
            if (gt.getGroup() == group) {
                groupTickets.add(new InvoiceLine("Group ticket " + gt.getId() + " (" + gt.getDate() + ", "
                        + gt.getTickets().size() + " members)", gt.getPrice()));
            }
        }
        InvoiceBuilder builder = builderFactory.get();
        return builder.forRecipient("GROUP", group.getName() + " (" + group.getId() + ")")
                .addSection("Group tickets", groupTickets)
                .build();
    }
}
