package es.upm.etsisi.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Joint ticket for the members of a group, composed of one {@link IndividualTicket} per member.
 * The composition is fixed at purchase time.
 */
public class GroupTicket extends Ticket {

    public static final String COMPONENT_SEPARATOR = ":";

    private Group group;
    private final GroupDiscount discount;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, BigDecimal basePrice, Group group, List<User> members) {
        super(id, date, basePrice);
        this.group = group;
        this.discount = GroupDiscount.forMembers(members.size());
        List<IndividualTicket> components = new ArrayList<>();
        for (User member : members) {
            components.add(new IndividualTicket(componentId(id, member), date, basePrice, member,
                    discount.getPercent()));
        }
        this.tickets = Collections.unmodifiableList(components);
    }

    public static String componentId(String groupTicketId, User member) {
        return groupTicketId + COMPONENT_SEPARATOR + member.getDni();
    }

    /**
     * @return the purchasing group, or {@code null} once that group has been deleted
     */
    public Group getGroup() {
        return group;
    }

    public void detachGroup() {
        this.group = null;
    }

    public GroupDiscount getDiscount() {
        return discount;
    }

    public List<IndividualTicket> getTickets() {
        return tickets;
    }

    public boolean contains(Ticket ticket) {
        return tickets.contains(ticket);
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : tickets) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            holders.add(ticket.getUser());
        }
        return holders;
    }

    @Override
    public List<String> getIdentifiers() {
        List<String> identifiers = new ArrayList<>(super.getIdentifiers());
        for (IndividualTicket ticket : tickets) {
            identifiers.add(ticket.getId());
        }
        return identifiers;
    }

    @Override
    public Optional<Ticket> findTicket(String identifier) {
        Optional<Ticket> self = super.findTicket(identifier);
        if (self.isPresent()) {
            return self;
        }
        for (IndividualTicket ticket : tickets) {
            if (ticket.getId().equals(identifier)) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }
}
