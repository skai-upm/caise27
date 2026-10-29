package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds group invoices: group identification and group tickets with their composition. Composition
 * tickets are shown as details only, so they are never added to the total twice.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public GroupInvoiceBuilder() {
        super("GROUP INVOICE");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        addHeaderLine("Group: " + group.getId());
        addHeaderLine("Name: " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder withGroupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket t : tickets) {
            List<String> details = new ArrayList<>();
            for (IndividualTicket part : t.getComposition()) {
                details.add(part.getId() + " " + part.getUser().getEmail() + " (" + part.getPrice().toPlainString()
                        + " EUR, " + t.getDiscountPercent() + "% discount)");
            }
            lines.add(new InvoiceLine("Group ticket " + t.getId() + " (" + t.getDate() + ", "
                    + t.getComposition().size() + " members)", t.getPrice(), details));
        }
        addSection("Group tickets", lines);
        return this;
    }
}
