package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.ParkFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice of a group: its group tickets with their composition. Composed tickets are described but only the group
 * ticket total is charged; members' passes are personal and not included.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(LocalDate issueDate, Group group, List<GroupTicket> groupTickets) {
        super(issueDate);
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    public void buildHeader() {
        setTitle("GROUP INVOICE");
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Group: " + group.getId());
        addRecipientLine("Name: " + group.getName());
    }

    @Override
    public void buildItems() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            List<String> composition = new ArrayList<>();
            for (IndividualTicket member : ticket.getComposition()) {
                composition.add(member.getId() + " " + member.getUser().getFirstName() + " "
                        + member.getUser().getSurnames() + " " + ParkFormat.formatPrice(member.getPrice()));
            }
            lines.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ParkFormat.formatDate(ticket.getDate())
                    + ", " + ticket.getComposition().size() + " member(s))", ticket.getPrice(), composition));
        }
        addSection("Group tickets", lines);
    }
}
