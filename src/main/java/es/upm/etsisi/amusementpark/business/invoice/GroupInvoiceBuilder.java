package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group invoice: identification and the group tickets with their composition.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        super("GROUP");
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Group: " + group.getIdentifier());
        addRecipientLine("Name: " + group.getName());
    }

    @Override
    public void buildSections() {
        buildGroupTicketSection();
    }

    private void buildGroupTicketSection() {
        List<InvoiceItem> items = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            List<String> composition = new ArrayList<>();
            for (IndividualTicket part : ticket.getIndividualTickets()) {
                composition.add(part.getIdentifier() + " " + part.getUser().getEmail() + " " + part.getUnitPrice());
            }
            items.add(new InvoiceItem("Group ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ", "
                    + ticket.getIndividualTickets().size() + " members)", ticket.getPrice(), composition));
        }
        addSection("Group tickets", items);
    }
}
