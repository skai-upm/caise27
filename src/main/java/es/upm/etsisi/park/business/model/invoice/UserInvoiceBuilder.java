package es.upm.etsisi.park.business.model.invoice;

import java.util.List;

import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.user.User;

/** Builds invoices for one user: direct tickets and personal passes. */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public static final String TICKETS = "Tickets";
    public static final String SPEED_PASSES = "SpeedPasses";
    public static final String SPECIAL_PASSES = "Special Passes";

    public UserInvoiceBuilder customer(User user) {
        recipientLine("Customer: " + user.getFullName());
        recipientLine("DNI: " + user.getDni());
        recipientLine("Email: " + user.getEmail());
        return this;
    }

    /** Individual tickets bought directly for the user (never member tickets of group tickets). */
    public UserInvoiceBuilder directTickets(List<IndividualTicket> tickets) {
        section(TICKETS);
        for (IndividualTicket ticket : tickets) {
            item("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice());
        }
        return this;
    }

    public UserInvoiceBuilder speedPasses(List<SpeedPass> passes) {
        section(SPEED_PASSES);
        addPasses(passes);
        return this;
    }

    public UserInvoiceBuilder specialPasses(List<SpecialPass> passes) {
        section(SPECIAL_PASSES);
        addPasses(passes);
        return this;
    }

    private void addPasses(List<? extends Pass> passes) {
        for (Pass pass : passes) {
            item(pass.getKind() + " " + pass.getId() + " for " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " [consumed]" : ""), pass.getPrice());
        }
    }
}
