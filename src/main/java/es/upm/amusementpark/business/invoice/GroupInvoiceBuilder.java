package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;

/**
 * Builds the invoice of a group: identification and group ticket purchases. The composition is
 * described but not charged again.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    static final String GROUP_TICKETS = "Group tickets";

    public GroupInvoiceBuilder() {
        super("INVOICE - GROUP");
    }

    public GroupInvoiceBuilder group(Group group) {
        header("Group: " + group.getId() + " - " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder groupTicket(GroupTicket ticket) {
        charge(GROUP_TICKETS, ticket.getId() + " (" + ticket.getDate() + ", " + ticket.individualTickets().size()
                + " members)", ticket.getPrice());
        for (IndividualTicket member : ticket.individualTickets()) {
            detail(GROUP_TICKETS, "  " + member.getId() + " " + member.getUser().getEmail() + " "
                    + member.getPrice().toPlainString() + " EUR");
        }
        return this;
    }
}
