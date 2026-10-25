package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket bought by a group, composed of one individual ticket per member at purchase time.
 */
public class GroupTicket extends Ticket implements GroupAccess {

    private final Group group;
    private final List<IndividualTicket> tickets = new ArrayList<>();
    private final BigDecimal price;

    public GroupTicket(String id, LocalDate date, Group group, BigDecimal price) {
        super(id, date);
        this.group = group;
        this.price = price;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return List.copyOf(tickets);
    }

    @Override
    public List<IndividualTicket> getIndividualTickets() {
        return getTickets();
    }

    public void addTicket(IndividualTicket ticket) {
        tickets.add(ticket);
        ticket.setGroupTicket(this);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            users.add(t.getUser());
        }
        return users;
    }

    /**
     * Discount percentage for a group of the given size.
     */
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

    /**
     * Total price for a number of members at the given individual price, after the discount.
     */
    public static BigDecimal totalPrice(BigDecimal individualPrice, int members) {
        BigDecimal gross = individualPrice.multiply(BigDecimal.valueOf(members));
        BigDecimal factor = BigDecimal.valueOf(100 - discountFor(members)).movePointLeft(2);
        return gross.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
