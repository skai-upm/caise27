package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Director of invoice construction: decides which builder steps apply to each recipient.
 */
public final class InvoiceController {

    private static InvoiceController instance;

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private PassRepository passes;
    private InvoiceBuilder builder;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        if (instance == null) {
            instance = new InvoiceController();
        }
        return instance;
    }

    /** Injects the repositories and the builder configured at application initialization. */
    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets,
            PassRepository passes, InvoiceBuilder builder) {
        this.passes = passes;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.builder = builder;
    }

    /** Invoice of the individual tickets bought directly for an active user. */
    public Invoice userInvoice(String email) throws NotFoundException {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        builder.forRecipient(Invoice.Type.USER)
                .recipient("Name", user.getFirstName() + " " + user.getSurnames())
                .recipient("DNI", user.getDni())
                .recipient("Email", user.getEmail())
                .section("Individual tickets");
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof IndividualTicket it && it.involves(user)) {
                builder.item(it.getId() + " (" + it.getDate() + ")", it.getPrice());
            }
        }
        addPasses(user, SpeedPass.class, "SpeedPasses");
        addPasses(user, SpecialPass.class, "Special passes");
        return builder.build();
    }

    /** Adds a section with the user's passes of the given type (on any of their individual tickets). */
    private void addPasses(User user, Class<? extends Pass> type, String title) {
        builder.section(title);
        for (Pass pass : passes.findAll()) {
            if (type.isInstance(pass) && pass.getTicket().involves(user)) {
                builder.item(pass.getId() + " (ticket " + pass.getTicket().getId() + ")", pass.getPrice());
            }
        }
    }

    /** Invoice of the group tickets bought by a group, with their composition as detail. */
    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = groups.findById(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found");
        }
        builder.forRecipient(Invoice.Type.GROUP)
                .recipient("Group", group.getId())
                .recipient("Name", group.getName())
                .section("Group tickets");
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                builder.item(gt.getId() + " (" + gt.getDate() + ", " + gt.getComposition().size() + " members)",
                        gt.getPrice());
                for (IndividualTicket component : gt.getComposition()) {
                    builder.detail(component.getId() + " " + component.getUser().getEmail() + ": "
                            + component.getPrice().toPlainString() + " EUR");
                }
            }
        }
        return builder.build();
    }
}
