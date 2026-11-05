package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupDiscount;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket purchase and deletion.
 */
public class TicketService {

    private final UserRepository users;
    private final TicketRepository tickets;
    private final GroupRepository groups;

    public TicketService(UserRepository users, TicketRepository tickets) {
        this(users, tickets, new GroupRepository());
    }

    public TicketService(UserRepository users, TicketRepository tickets, GroupRepository groups) {
        this.users = users;
        this.tickets = tickets;
        this.groups = groups;
    }

    public IndividualTicket createTicket(String identifier, String email, LocalDate date) {
        Park park = Park.getInstance();
        tickets.ensureAvailable(List.of(identifier));
        User user = users.findByEmail(email).orElseThrow(() -> new OperationException("user not found"));
        requireNotPast(date, park.getCurrentDate());
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, park.getCurrentPrice());
        tickets.add(ticket);
        return ticket;
    }

    public void deleteTicket(String identifier) {
        Ticket ticket = tickets.findById(identifier)
                .filter(IndividualTicket.class::isInstance)
                .orElseThrow(() -> new OperationException("ticket '" + identifier + "' not found"));
        if (tickets.containerOf((IndividualTicket) ticket).isPresent()) {
            throw new OperationException("ticket '" + identifier + "' belongs to a group ticket; delete the group ticket instead");
        }
        requireDeletable(ticket);
        tickets.remove(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, LocalDate date) {
        Park park = Park.getInstance();
        Group group = groups.findById(groupIdentifier)
                .orElseThrow(() -> new OperationException("group '" + groupIdentifier + "' not found"));
        if (group.isEmpty()) {
            throw new OperationException("group '" + groupIdentifier + "' has no members");
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
                .orElseThrow(() -> new OperationException("group ticket '" + identifier + "' not found"));
        requireDeletable(ticket);
        List<Ticket> all = new ArrayList<>(ticket.getComposition());
        all.add(ticket);
        tickets.removeAll(all);
    }

    static void requireNotPast(LocalDate date, LocalDate today) {
        if (date.isBefore(today)) {
            throw new OperationException("tickets cannot be created for past dates (current date " + today + ")");
        }
    }

    static void requireDeletable(Ticket ticket) {
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isDeletableOn(today)) {
            throw new OperationException("only tickets dated after the current date (" + today + ") can be deleted");
        }
    }
}
