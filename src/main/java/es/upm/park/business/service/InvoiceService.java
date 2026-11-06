package es.upm.park.business.service;

import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.invoice.InvoiceBuilder;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.GroupRepository;
import es.upm.park.persistence.repository.PassRepository;
import es.upm.park.persistence.repository.TicketRepository;
import es.upm.park.persistence.repository.UserRepository;

/**
 * Director of invoice construction: decides which builder steps apply to
 * each recipient, from the purchases recorded in the system.
 */
public class InvoiceService {

    static final String TICKETS_SECTION = "Individual tickets";
    static final String GROUP_TICKETS_SECTION = "Group tickets";
    static final String SPEED_PASSES_SECTION = "SpeedPasses";
    static final String SPECIAL_PASSES_SECTION = "Special passes";

    private static final InvoiceService INSTANCE = new InvoiceService();

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private InvoiceBuilder builder;
    private PassRepository passes = new PassRepository();

    private InvoiceService() {
    }

    /** The single InvoiceService instance of the execution. */
    public static InvoiceService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies chosen during application initialization.
     *
     * @return this controller
     */
    public InvoiceService configure(UserRepository users, GroupRepository groups, TicketRepository tickets,
            InvoiceBuilder builder) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.builder = builder;
        return this;
    }

    /**
     * Injects the passes so user invoices can include them.
     *
     * @return this controller
     */
    public InvoiceService withPasses(PassRepository passRepository) {
        this.passes = passRepository;
        return this;
    }

    /** Invoice of an active user: direct individual tickets (no group constituents). */
    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new UserException("no user with email " + email));
        builder.reset("USER")
                .recipient(user.getFirstName() + " " + user.getSurnames())
                .recipient("DNI: " + user.getDni())
                .recipient("Email: " + user.getEmail())
                .section(TICKETS_SECTION);
        for (Ticket t : tickets.findAll()) {
            if (t instanceof IndividualTicket i && i.involves(user)) {
                builder.item("Ticket " + i.getId() + " (" + i.getDate() + ")", i.getPrice());
            }
        }
        addPasses(user, SpeedPass.class, SPEED_PASSES_SECTION, "SpeedPass ");
        addPasses(user, SpecialPass.class, SPECIAL_PASSES_SECTION, "Special pass ");
        return builder.build();
    }

    /** Invoice of an existing group: its group tickets, composition as detail. */
    public Invoice groupInvoice(String id) {
        Group group = groups.findById(id).orElseThrow(() -> new GroupException("no group " + id));
        builder.reset("GROUP")
                .recipient("Group " + group.getId() + " - " + group.getName())
                .section(GROUP_TICKETS_SECTION);
        for (Ticket t : tickets.findAll()) {
            if (t instanceof GroupTicket g && g.getGroup() == group) {
                builder.item("Group ticket " + g.getId() + " (" + g.getDate() + ", " + g.getComposition().size()
                        + " members)", g.getPrice());
                for (IndividualTicket c : g.getComposition()) {
                    builder.detail(c.getId() + " " + c.getUser().getEmail() + " " + c.getPrice().toPlainString());
                }
            }
        }
        return builder.build();
    }

    private void addPasses(User user, Class<? extends Pass> type, String section, String label) {
        builder.section(section);
        for (Pass p : passes.findAll()) {
            if (type.isInstance(p) && p.getTicket().involves(user)) {
                builder.item(label + p.getId() + " (ticket " + p.getTicket().getId() + ")", p.getPrice());
            }
        }
    }
}
