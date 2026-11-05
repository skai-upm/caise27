package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.RuleViolationException;


import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupDiscount;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket purchase and deletion.
 */
public class TicketService {

    private UserRepository users;
    private TicketRepository tickets;
    private GroupRepository groups;
    private PassRepository passes = new PassRepository();

    private TicketService() {
    }

    private static final class Holder {
        private static final TicketService INSTANCE = new TicketService();
    }

    /** The single instance of this controller. */
    public static TicketService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    /** Injects the pass repository, whose passes are removed together with deleted tickets. */
    public TicketService injectPasses(PassRepository passes) {
        this.passes = passes;
        return this;
    }

    public TicketService inject(UserRepository users, TicketRepository tickets, GroupRepository groups) {
        this.users = users;
        this.tickets = tickets;
        this.groups = groups;
        return this;
    }



    public IndividualTicket createTicket(String identifier, String email, LocalDate date) {
        Park park = Park.getInstance();
        tickets.ensureAvailable(List.of(identifier));
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("user not found"));
        requireNotPast(date, park.getCurrentDate());
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, park.getCurrentPrice());
        tickets.add(ticket);
        return ticket;
    }

    public void deleteTicket(String identifier) {
        Ticket ticket = tickets.findById(identifier)
                .filter(IndividualTicket.class::isInstance)
                .orElseThrow(() -> new NotFoundException("ticket '" + identifier + "' not found"));
        if (tickets.containerOf((IndividualTicket) ticket).isPresent()) {
            throw new RuleViolationException("ticket '" + identifier + "' belongs to a group ticket; delete the group ticket instead");
        }
        requireDeletable(ticket);
        passes.removeAll(passes.passesOf((IndividualTicket) ticket));
        tickets.remove(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, LocalDate date) {
        Park park = Park.getInstance();
        Group group = groups.findById(groupIdentifier)
                .orElseThrow(() -> new NotFoundException("group '" + groupIdentifier + "' not found"));
        if (group.isEmpty()) {
            throw new RuleViolationException("group '" + groupIdentifier + "' has no members");
        }
        requireNotPast(date, park.getCurrentDate());
        List<String> ids = new ArrayList<>();
        ids.add(identifier);
        group.getMembers().forEach(u -> ids.add(GroupTicket.compositionIdentifier(identifier, u.getDni())));
        tickets.ensureAvailable(ids);
        BigDecimal unitPrice = GroupDiscount.forMembers(group.getMembers().size()).apply(park.getCurrentPrice());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : group.getMembers()) {
            composition.add(new IndividualTicket(GroupTicket.compositionIdentifier(identifier, member.getDni()),
                    date, member, unitPrice));
        }
        GroupTicket groupTicket = new GroupTicket(identifier, date, group, composition);
        List<Ticket> all = new ArrayList<>(composition);
        all.add(groupTicket);
        tickets.addAll(all);
        return groupTicket;
    }

    public void deleteGroupTicket(String identifier) {
        GroupTicket ticket = tickets.findById(identifier)
                .filter(GroupTicket.class::isInstance).map(GroupTicket.class::cast)
                .orElseThrow(() -> new NotFoundException("group ticket '" + identifier + "' not found"));
        requireDeletable(ticket);
        ticket.getComposition().forEach(c -> passes.removeAll(passes.passesOf(c)));
        List<Ticket> all = new ArrayList<>(ticket.getComposition());
        all.add(ticket);
        tickets.removeAll(all);
    }

    static void requireNotPast(LocalDate date, LocalDate today) {
        if (date.isBefore(today)) {
            throw new RuleViolationException("tickets cannot be created for past dates (current date " + today + ")");
        }
    }

    static void requireDeletable(Ticket ticket) {
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isDeletableOn(today)) {
            throw new RuleViolationException("only tickets dated after the current date (" + today + ") can be deleted");
        }
    }
}
