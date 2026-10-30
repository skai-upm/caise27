package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;

import java.util.List;

/**
 * Builds a group's invoice: group identification and group tickets with their composition.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public GroupInvoiceBuilder() {
        super("INVOICE - GROUP");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        addRecipientLine("Group: " + group.getId());
        addRecipientLine("Name: " + group.getName());
        return this;
    }

    /**
     * Charges each group ticket once and describes its composition without charging it again.
     */
    public GroupInvoiceBuilder withGroupTickets(List<GroupTicket> tickets) {
        addSection("Group tickets", tickets.stream()
                .map(t -> new InvoiceItem("Group ticket " + t.getId() + " (" + t.getDate() + ", "
                        + t.getIndividualTickets().size() + " member(s))", t.getPrice()))
                .toList(), true);
        for (GroupTicket ticket : tickets) {
            addSection("Composition of " + ticket.getId(), ticket.getIndividualTickets().stream()
                    .map(i -> new InvoiceItem(i.getId() + " - " + i.getUser().getFirstName() + " "
                            + i.getUser().getSurnames(), i.getPrice()))
                    .toList(), false);
        }
        return this;
    }
}
