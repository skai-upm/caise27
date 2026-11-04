package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Creates and deletes individual and group tickets. Date rules always use the date supplied
 * by the controller (the configured system date). Every successful change is saved before
 * it becomes visible in memory.
 */
public class TicketManager {

    private final TicketFileStore store;
    private final GroupTicketFileStore groupStore;
    private final UserManager users;
    private final List<String> historicalLines;
    private final List<String> groupHistoricalLines;
    private final Set<String> historicalIds = new HashSet<>();
    private List<IndividualTicket> tickets;
    private List<GroupTicket> groupTickets;

    public TicketManager(TicketFileStore store, GroupTicketFileStore groupStore,
                         UserManager users, GroupManager groups) throws IOException {
        this.store = store;
        this.groupStore = groupStore;
        this.users = users;
        TicketFileStore.Contents contents = store.load(users::findByDni);
        this.tickets = new ArrayList<>(contents.tickets());
        this.historicalLines = new ArrayList<>(contents.historicalLines());
        GroupTicketFileStore.Contents groupContents = groupStore.load(groups::find, users::findByDni);
        this.groupTickets = new ArrayList<>(groupContents.groupTickets());
        this.groupHistoricalLines = new ArrayList<>(groupContents.historicalLines());
        for (String line : historicalLines) {
            historicalIds.add(line.split(";", -1)[0]);
        }
        for (String line : groupHistoricalLines) {
            historicalIds.addAll(GroupTicketFileStore.idsOf(line));
        }
    }

    /** Individual tickets bought on their own. */
    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return Collections.unmodifiableList(groupTickets);
    }

    /** Every individual and group ticket through the common abstraction. */
    public List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>(tickets);
        all.addAll(groupTickets);
        return all;
    }

    public Optional<IndividualTicket> find(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /** Ticket usable for attraction access: a standalone individual ticket or a group ticket. */
    public Optional<Ticket> findForAccess(String id) {
        Optional<Ticket> individual = find(id).map(Ticket.class::cast);
        return individual.isPresent() ? individual : findGroupTicket(id).map(Ticket.class::cast);
    }

    public Optional<GroupTicket> findGroupTicket(String id) {
        return groupTickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Result create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        if (!UserValidator.isNonBlank(id)) {
            return Result.error("Ticket identifier must not be empty.");
        }
        if (isIdTaken(id)) {
            return Result.error("A ticket with identifier " + id + " already exists.");
        }
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty()) {
            return Result.error("No registered user with email " + email + ".");
        }
        if (date.isBefore(today)) {
            return Result.error("Ticket date " + date + " is before the system date " + today + ".");
        }
        List<IndividualTicket> candidate = new ArrayList<>(tickets);
        candidate.add(new IndividualTicket(id, date, price, user.get()));
        try {
            store.save(candidate, historicalLines);
        } catch (IOException e) {
            return Result.error("Could not save tickets: " + e.getMessage());
        }
        tickets = candidate;
        return Result.verbatim(true, "OK TICKET CREATED -> " + price.toPlainString());
    }

    public Result delete(String id, LocalDate today) {
        Optional<IndividualTicket> ticket = find(id);
        if (ticket.isEmpty()) {
            boolean constituent = groupTickets.stream()
                    .anyMatch(g -> g.getTickets().stream().anyMatch(t -> t.getId().equals(id)));
            return Result.error(constituent
                    ? "Ticket " + id + " belongs to a group ticket; delete the group ticket instead."
                    : "Ticket " + id + " does not exist.");
        }
        if (!ticket.get().getDate().isAfter(today)) {
            return Result.error("Ticket " + id + " can only be deleted if its date is later than the system date " + today + ".");
        }
        List<IndividualTicket> candidate = new ArrayList<>(tickets);
        candidate.remove(ticket.get());
        try {
            store.save(candidate, historicalLines);
        } catch (IOException e) {
            return Result.error("Could not save tickets: " + e.getMessage());
        }
        tickets = candidate;
        return Result.ok("Ticket " + id + " deleted.");
    }

    public Result createGroupTicket(String id, Group group, LocalDate date, LocalDate today, BigDecimal price) {
        if (!UserValidator.isNonBlank(id)) {
            return Result.error("Ticket identifier must not be empty.");
        }
        if (group.getMembers().isEmpty()) {
            return Result.error("Group " + group.getId() + " has no members.");
        }
        if (date.isBefore(today)) {
            return Result.error("Ticket date " + date + " is before the system date " + today + ".");
        }
        GroupTicket ticket = GroupTicket.purchase(id, date, group, price);
        if (isIdTaken(id) || ticket.getTickets().stream().anyMatch(t -> isIdTaken(t.getId()))) {
            return Result.error("A ticket with identifier " + id + " already exists.");
        }
        List<GroupTicket> candidate = new ArrayList<>(groupTickets);
        candidate.add(ticket);
        Result saved = saveGroups(candidate);
        return saved.success()
                ? Result.verbatim(true, "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString())
                : saved;
    }

    public Result deleteGroupTicket(String id, LocalDate today) {
        Optional<GroupTicket> ticket = findGroupTicket(id);
        if (ticket.isEmpty()) {
            return Result.error("Group ticket " + id + " does not exist.");
        }
        if (!ticket.get().getDate().isAfter(today)) {
            return Result.error("Group ticket " + id + " can only be deleted if its date is later than the system date " + today + ".");
        }
        List<GroupTicket> candidate = new ArrayList<>(groupTickets);
        candidate.remove(ticket.get());
        Result saved = saveGroups(candidate);
        return saved.success() ? Result.ok("Group ticket " + id + " and its individual tickets deleted.") : saved;
    }

    /** Whether the user owns a ticket (individual or inside a group ticket) dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        boolean individual = tickets.stream()
                .anyMatch(t -> t.getUser() == user && !t.getDate().isBefore(today));
        return individual || groupTickets.stream()
                .filter(g -> !g.getDate().isBefore(today))
                .flatMap(g -> g.getTickets().stream())
                .anyMatch(t -> t.getUser() == user);
    }

    /** Whether the group has a group ticket dated on or after {@code today}. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        return groupTickets.stream().anyMatch(g -> g.getGroup() == group && !g.getDate().isBefore(today));
    }

    private boolean isIdTaken(String id) {
        if (historicalIds.contains(id) || find(id).isPresent() || findGroupTicket(id).isPresent()) {
            return true;
        }
        return groupTickets.stream().anyMatch(g -> g.getTickets().stream().anyMatch(t -> t.getId().equals(id)));
    }

    private Result saveGroups(List<GroupTicket> candidate) {
        try {
            groupStore.save(candidate, groupHistoricalLines);
        } catch (IOException e) {
            return Result.error("Could not save group tickets: " + e.getMessage());
        }
        groupTickets = candidate;
        return Result.ok("Group tickets saved.");
    }
}
