package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/** Builds a user's invoice from the individual tickets bought directly for that user. */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> tickets;

    /**
     * @param tickets tickets bought with TICKET CREATE for this user; tickets generated
     *                by a group ticket must not be passed here
     */
    public UserInvoiceBuilder(User user, List<IndividualTicket> tickets) {
        this.user = user;
        this.tickets = List.copyOf(tickets);
    }

    @Override
    public void buildHeader() {
        setTitle("Invoice for user " + user.getName() + " " + user.getSurname() + " <" + user.getEmail()
                + "> (DNI " + user.getDni() + ")");
    }

    @Override
    public void buildTicketSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()));
        }
        addSection("Individual tickets", lines);
    }
}
