package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.User;

import java.util.List;

/**
 * Builds user invoices: identification, directly purchased tickets and personal passes.
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    public UserInvoiceBuilder forUser(User user) {
        title("USER INVOICE");
        headerLine("USER: " + user.getFirstName() + " " + user.getSurnames());
        headerLine("DNI: " + user.getDni());
        headerLine("EMAIL: " + user.getEmail());
        return this;
    }

    public UserInvoiceBuilder directTickets(List<IndividualTicket> tickets) {
        section("TICKETS");
        for (IndividualTicket t : tickets) {
            item("TICKET " + t.getId() + " (" + t.getDate() + ")", t.getPrice());
        }
        return this;
    }

    public UserInvoiceBuilder speedPasses(List<? extends Pass> passes) {
        return passes("SPEEDPASSES", "SPEEDPASS", passes);
    }

    public UserInvoiceBuilder specialPasses(List<? extends Pass> passes) {
        return passes("SPECIAL PASSES", "SPECIAL PASS", passes);
    }

    private UserInvoiceBuilder passes(String sectionTitle, String label, List<? extends Pass> passes) {
        section(sectionTitle);
        for (Pass p : passes) {
            String state = p.isConsumed() ? "consumed" : "unused";
            item(label + " " + p.getId() + " for ticket " + p.getTicket().getId() + " (" + state + ")", p.getPrice());
        }
        return this;
    }
}
