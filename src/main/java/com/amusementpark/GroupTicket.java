package com.amusementpark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ticket bought by a group, composed of one individual ticket per member at purchase time.
 */
public class GroupTicket extends Ticket {
    private final Group group;
    private final List<IndividualTicket> tickets;

    public GroupTicket(String id, LocalDate date, BigDecimal price, Group group,
                       List<IndividualTicket> tickets) {
        super(id, date, price);
        this.group = group;
        this.tickets = new ArrayList<>(tickets);
    }

    /**
     * Buys a group ticket for the group's current members at the given individual price.
     */
    public static GroupTicket purchase(String id, LocalDate date, BigDecimal individualPrice, Group group) {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (User member : group.getMembers()) {
            tickets.add(new IndividualTicket(memberTicketId(id, tickets.size() + 1), date, individualPrice, member));
        }
        return new GroupTicket(id, date, totalPrice(individualPrice, tickets.size()), group, tickets);
    }

    public static String memberTicketId(String groupTicketId, int number) {
        return groupTicketId + "-" + number;
    }

    /**
     * Discount applied to the sum of individual prices for the given number of members.
     */
    public static BigDecimal discountFor(int members) {
        if (members < 3) {
            return BigDecimal.ZERO;
        }
        if (members <= 5) {
            return new BigDecimal("0.10");
        }
        if (members <= 10) {
            return new BigDecimal("0.20");
        }
        return new BigDecimal("0.50");
    }

    public static BigDecimal totalPrice(BigDecimal individualPrice, int members) {
        BigDecimal sum = individualPrice.multiply(BigDecimal.valueOf(members));
        return sum.multiply(BigDecimal.ONE.subtract(discountFor(members))).setScale(2, RoundingMode.HALF_UP);
    }

    public Group getGroup() {
        return group;
    }

    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            users.add(ticket.getUser());
        }
        return users;
    }
}
