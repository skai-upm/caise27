package es.upm.parque.business.controller;

/**
 * The application's controllers, configured at startup and handed together to the presentation layer.
 */
public class Controllers {

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final AttractionManager attractions;
    private final InvoiceManager invoices;
    private final PassManager passes;

    public Controllers(UserManager users, GroupManager groups, TicketManager tickets, AttractionManager attractions,
                       InvoiceManager invoices, PassManager passes) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.invoices = invoices;
        this.passes = passes;
    }

    public UserManager users() {
        return users;
    }

    public GroupManager groups() {
        return groups;
    }

    public TicketManager tickets() {
        return tickets;
    }

    public AttractionManager attractions() {
        return attractions;
    }

    public InvoiceManager invoices() {
        return invoices;
    }

    public PassManager passes() {
        return passes;
    }
}
