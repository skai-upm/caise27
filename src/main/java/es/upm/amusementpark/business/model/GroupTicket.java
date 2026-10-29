package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A joint ticket bought by a group, composed of one individual ticket per
 * member at purchase time. Its price is the sum of the composed tickets.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final List<IndividualTicket> composition;
    private Group group;

    public GroupTicket(String identifier, Group group, LocalDate date, List<IndividualTicket> composition) {
        super(identifier, date, total(composition));
        this.group = group;
        this.composition = List.copyOf(composition);
    }

    private static BigDecimal total(List<IndividualTicket> composition) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : composition) {
            sum = sum.add(ticket.getPrice());
        }
        return sum;
    }

    /** Discount percentage for the number of users included in the purchase. */
    public static int discountPercent(int members) {
        if (members < 3) {
            return 0;
        }
        if (members <= 5) {
            return 10;
        }
        if (members <= 10) {
            return 20;
        }
        return 50;
    }

    /** Price of each individual ticket of a group ticket with that many members. */
    public static BigDecimal discountedPrice(BigDecimal basePrice, int members) {
        BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(discountPercent(members)));
        return basePrice.multiply(factor).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Identifier of the composed ticket of the user with the given DNI. */
    public static String componentIdentifier(String groupTicketIdentifier, String dni) {
        return groupTicketIdentifier + ":" + dni;
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    /** @return the purchasing group, or {@code null} once it has been deleted */
    public Group getGroup() {
        return group;
    }

    /** Clears the reference to the purchasing group when that group is deleted. */
    public void releaseGroup() {
        group = null;
    }

    @Override
    public Collection<String> identifiers() {
        List<String> ids = new ArrayList<>();
        ids.add(getIdentifier());
        for (IndividualTicket ticket : composition) {
            ids.add(ticket.getIdentifier());
        }
        return ids;
    }

    @Override
    public List<IndividualTicket> individualTickets() {
        return composition;
    }

    @Override
    public List<User> admittedUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            users.add(ticket.getUser());
        }
        return users;
    }

    @Override
    public boolean involves(User user) {
        for (IndividualTicket ticket : composition) {
            if (ticket.involves(user)) {
                return true;
            }
        }
        return false;
    }
}
