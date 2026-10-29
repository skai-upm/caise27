package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.ticket.GroupTicket;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice: identifier, name and group tickets with their composition. The composition
 * is shown as detail only; members' passes are personal purchases and are not part of this invoice.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    public static final String GROUP_TICKETS = "Group tickets";

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
        reset();
    }

    @Override
    public void buildHeader() {
        setTitle("INVOICE - GROUP");
        addHeaderLine("Group: " + group.getId());
        addHeaderLine("Name: " + group.getName());
    }

    @Override
    public void buildTicketItems() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            List<String> composition = new ArrayList<>();
            for (IndividualTicket member : ticket.getComposition()) {
                composition.add(member.getId() + " " + member.getUser().getEmail() + " "
                        + member.getPrice().toPlainString() + " EUR");
            }
            lines.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.getComposition().size() + " members)", ticket.getPrice(), composition));
        }
        addSection(GROUP_TICKETS, lines);
    }

    /**
     * Group invoices have no pass items: passes belong to the members' personal invoices.
     */
    @Override
    public void buildPassItems() {
        // Intentionally empty.
    }
}
