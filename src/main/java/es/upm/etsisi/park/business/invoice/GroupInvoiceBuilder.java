package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Money;
import es.upm.etsisi.park.business.ValueParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group invoice: each group ticket is charged once at its total, with its composition
 * shown as non-charged detail. Members' personal passes are not included.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    public void buildHeader() {
        setTitle("GROUP INVOICE");
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Group: " + group.getId() + " - " + group.getName());
    }

    @Override
    public void buildPurchases() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket g : groupTickets) {
            int discount = g.getComposition().get(0).getDiscountPercent();
            lines.add(new ChargeLine("Group ticket " + g.getId() + " (" + ValueParser.formatDate(g.getDate()) + ", "
                    + g.getComposition().size() + " members, " + discount + "% discount)", g.getPrice()));
            for (IndividualTicket c : g.getComposition()) {
                lines.add(new DetailLine(c.getId() + " " + c.getUser().getFirstName() + " " + c.getUser().getSurnames()
                        + " " + Money.format(c.getPrice())));
            }
        }
        addSection("Group tickets", lines);
    }
}
