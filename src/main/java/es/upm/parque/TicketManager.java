package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for individual and group tickets, which share one identifier space.
 * All date checks use the shared system date.
 */
public class TicketManager {

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private final TicketRepository repository;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final SystemDate systemDate;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private BigDecimal currentPrice = DEFAULT_PRICE;

    public TicketManager(TicketRepository repository, UserManager userManager, GroupManager groupManager,
                         SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.systemDate = systemDate;
        for (Ticket ticket : repository.loadAll(userManager::findByDni, groupManager::find)) {
            tickets.put(ticket.getId(), ticket);
        }
    }

    public Ticket find(String id) {
        return tickets.get(id);
    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the price of individual tickets sold from now on. It must be positive with at most two decimals.
     */
    public void setCurrentPrice(String amount) {
        BigDecimal price;
        try {
            price = new BigDecimal(amount.trim());
        } catch (NumberFormatException e) {
            throw new UserException("Invalid price: " + amount);
        }
        if (price.signum() <= 0 || price.scale() > 2) {
            throw new UserException("Invalid price: " + amount + " (must be positive with at most two decimals)");
        }
        currentPrice = price.setScale(2);
    }

    public IndividualTicket create(String id, String email, String dateText) {
        requireNewId(id);
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new UserException("No user with email " + email);
        }
        LocalDate date = parseUsableDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, currentPrice);
        tickets.put(id, ticket);
        save();
        return ticket;
    }

    public void delete(String id) {
        if (!(tickets.get(id) instanceof IndividualTicket ticket)) {
            throw new UserException("No individual ticket with identifier " + id);
        }
        GroupTicket owner = findOwner(ticket);
        if (owner != null) {
            throw new UserException("Ticket " + id + " belongs to group ticket " + owner.getId()
                    + " and cannot be deleted on its own");
        }
        requireFuture(ticket);
        tickets.remove(id);
        save();
    }

    /**
     * Buys a group ticket, generating one individual ticket per current member of the group.
     */
    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        requireNewId(id);
        Group group = groupManager.find(groupId);
        if (group == null) {
            throw new UserException("No group with identifier " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new UserException("Group " + groupId + " has no members");
        }
        for (int i = 1; i <= members.size(); i++) {
            requireNewId(internalId(id, i));
        }
        LocalDate date = parseUsableDate(dateText);
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            IndividualTicket ticket = new IndividualTicket(internalId(id, i + 1), date, members.get(i), currentPrice);
            composition.add(ticket);
            tickets.put(ticket.getId(), ticket);
        }
        GroupTicket groupTicket = new GroupTicket(id, date, group, composition);
        tickets.put(id, groupTicket);
        save();
        return groupTicket;
    }

    /**
     * Deletes a future group ticket together with the individual tickets it is composed of.
     */
    public void deleteGroupTicket(String id) {
        if (!(tickets.get(id) instanceof GroupTicket groupTicket)) {
            throw new UserException("No group ticket with identifier " + id);
        }
        requireFuture(groupTicket);
        groupTicket.getTickets().forEach(ticket -> tickets.remove(ticket.getId()));
        tickets.remove(id);
        save();
    }

    /**
     * True if the user holds an individual ticket (standalone or from a group) dated on or after the system date.
     */
    public boolean hasCurrentOrFutureTicket(User user) {
        LocalDate today = systemDate.get();
        return tickets.values().stream()
                .anyMatch(ticket -> ticket instanceof IndividualTicket individual
                        && individual.getUser() == user && individual.isCurrentOrFuture(today));
    }

    /**
     * True if the group has a group ticket dated on or after the system date, so it cannot be modified.
     */
    public boolean isGroupLocked(Group group) {
        LocalDate today = systemDate.get();
        return tickets.values().stream()
                .anyMatch(ticket -> ticket instanceof GroupTicket groupTicket
                        && groupTicket.getGroup() == group && groupTicket.isCurrentOrFuture(today));
    }

    /**
     * Returns the group ticket that contains the given individual ticket, or {@code null} if it is standalone.
     */
    public GroupTicket findOwner(IndividualTicket ticket) {
        for (Ticket candidate : tickets.values()) {
            if (candidate instanceof GroupTicket groupTicket && groupTicket.contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    private static String internalId(String groupTicketId, int position) {
        return groupTicketId + "-" + position;
    }

    private void requireNewId(String id) {
        if (id == null || id.isBlank()) {
            throw new UserException("The ticket identifier cannot be empty");
        }
        if (tickets.containsKey(id)) {
            throw new UserException("A ticket with identifier " + id + " already exists");
        }
    }

    private LocalDate parseUsableDate(String dateText) {
        LocalDate date = SystemDate.parse(dateText.trim());
        if (date.isBefore(systemDate.get())) {
            throw new UserException("Cannot buy a ticket for a past date: " + dateText);
        }
        return date;
    }

    private void requireFuture(Ticket ticket) {
        if (!ticket.isFuture(systemDate.get())) {
            throw new UserException("Ticket " + ticket.getId() + " can only be deleted before its date");
        }
    }

    private void save() {
        try {
            repository.saveAll(tickets.values());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save tickets", e);
        }
    }
}
