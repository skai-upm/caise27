package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member
 * included in the purchase. It references the purchasing group while that
 * group exists.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private Group group;
    private final List<IndividualTicket> composition;

    public GroupTicket(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
        super(identifier, date);
        this.group = group;
        this.composition = List.copyOf(composition);
        for (IndividualTicket ticket : this.composition) {
            ticket.assignTo(this);
        }
    }

    /** The purchasing group, or {@code null} if it has been deleted. */
    public Group getGroup() {
        return group;
    }

    public void clearGroup() {
        this.group = null;
    }

    @Override
    public List<IndividualTicket> getComposition() {
        return composition;
    }

    /** The total price: the sum of the prices of the composed tickets. */
    @Override
    public BigDecimal getPrice() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (IndividualTicket ticket : composition) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    /** The users of the composed tickets. */
    @Override
    public List<User> getHolders() {
        List<User> holders = new ArrayList<>();
        for (IndividualTicket ticket : composition) {
            holders.add(ticket.getUser());
        }
        return holders;
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return composition;
    }

    /** Discount percentage for a purchase including the given number of users. */
    public static int discountFor(int members) {
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

    /** Price of each composed ticket: the base price minus the group discount. */
    public static BigDecimal discountedPrice(BigDecimal basePrice, int members) {
        BigDecimal factor = BigDecimal.valueOf(100 - discountFor(members));
        return basePrice.multiply(factor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
