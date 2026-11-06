package es.upm.park.business.invoice;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;

import java.util.List;

/** Builds invoices for a user: identification, direct tickets and personal passes. */
public class UserInvoiceBuilder extends InvoiceBuilder<UserInvoiceBuilder> {

    public UserInvoiceBuilder() {
        title("INVOICE - USER");
    }

    @Override
    protected UserInvoiceBuilder self() {
        return this;
    }

    public UserInvoiceBuilder forUser(String email, String dni) {
        return recipient("User", email).recipient("DNI", dni);
    }

    /** Individual tickets bought directly; tickets composing a group ticket are not the user's purchase. */
    public UserInvoiceBuilder directTickets(List<IndividualTicket> tickets) {
        startSection("Individual tickets");
        tickets.stream()
                .filter(ticket -> !ticket.isPartOfGroupTicket())
                .forEach(ticket -> item(ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        return this;
    }

    /** SpeedPasses of any of the user's individual tickets (also composed ones). */
    public UserInvoiceBuilder speedPasses(List<? extends Pass> passes) {
        return passSection("SpeedPasses", passes.stream().filter(SpeedPass.class::isInstance).toList());
    }

    /** Special Passes of any of the user's individual tickets (also composed ones). */
    public UserInvoiceBuilder specialPasses(List<? extends Pass> passes) {
        return passSection("Special Passes", passes.stream().filter(SpecialPass.class::isInstance).toList());
    }

    private UserInvoiceBuilder passSection(String title, List<? extends Pass> passes) {
        startSection(title);
        passes.forEach(pass -> item(pass.getId() + " (ticket " + pass.getTicket().getId() + ")", pass.getPrice()));
        return this;
    }
}
