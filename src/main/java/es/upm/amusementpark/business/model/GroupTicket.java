package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket purchased by a group, composed of one individual ticket per member at purchase time.
 * The composition never changes. The group reference becomes null if the group is deleted.
 */
public class GroupTicket extends Ticket {

    private final List<IndividualTicket> composition;
    private final String groupIdentifier;
    private Group group;

    public GroupTicket(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
        this(identifier, date, group.getIdentifier(), group, composition);
    }

    /** Rebuilds a group ticket; {@code group} may be null when the purchasing group no longer exists. */
    public GroupTicket(String identifier, LocalDate date, String groupIdentifier, Group group,
                       List<IndividualTicket> composition) {
        super(identifier, date);
        this.group = group;
        this.groupIdentifier = groupIdentifier;
        this.composition = List.copyOf(composition);
    }

    /** Builds the identifier of the composition ticket of a member. */
    public static String compositionIdentifier(String groupTicketIdentifier, String dni) {
        return groupTicketIdentifier + ":" + dni;
    }

    public List<IndividualTicket> getComposition() {
        return composition;
    }

    /** Purchasing group, or null when it no longer exists. */
    public Group getGroup() {
        return group;
    }

    public String getGroupIdentifier() {
        return groupIdentifier;
    }

    public void detachGroup() {
        this.group = null;
    }

    public boolean contains(IndividualTicket ticket) {
        return composition.contains(ticket);
    }

    /** All users of the composition enter together. */
    @Override
    public List<User> admittedUsers() {
        return composition.stream().map(IndividualTicket::getUser).toList();
    }

    /** Total price: sum of the composition tickets' prices. */
    @Override
    public BigDecimal getPrice() {
        return composition.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
