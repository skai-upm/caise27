package es.upm.etsisi.park.model.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.upm.etsisi.park.model.group.Group;
import es.upm.etsisi.park.user.User;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private static User user(int n) {
        return new User("U" + n, "S", String.format("%08dX", n), "u" + n + "@correo.com", "Clave1!", "600", "1234567890123456");
    }

    private static GroupTicket groupTicket(int size, String basePrice) {
        GroupDiscount discount = GroupDiscount.forMembers(size);
        List<IndividualTicket> members = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            User u = user(i);
            members.add(new IndividualTicket(GroupTicket.memberId("TG", u.getDni()), DAY, u,
                    new BigDecimal(basePrice), discount.getPercent()));
        }
        return new GroupTicket("TG", DAY, new Group("G", "G"), members);
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "30,50"})
    void discountBrackets(int members, int percent) {
        assertEquals(percent, GroupDiscount.forMembers(members).getPercent());
    }

    @Test
    void sixMembersAtFortyCost192() {
        GroupTicket ticket = groupTicket(6, "40.00");
        assertEquals(new BigDecimal("32.00"), ticket.getMembers().get(0).getPrice());
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertEquals("192.00 EUR", ticket.getFormattedPrice());
    }

    @Test
    void twoMembersAtFortyFiftyFive() {
        assertEquals(new BigDecimal("91.00"), groupTicket(2, "45.50").getPrice());
    }

    @Test
    void elevenMembersHalfPrice() {
        assertEquals(new BigDecimal("220.00"), groupTicket(11, "40").getPrice());
    }

    @Test
    void ticketsShareAbstraction() {
        List<Ticket> tickets = List.of(new IndividualTicket("T1", DAY, user(1), new BigDecimal("40")),
                groupTicket(3, "40"));
        assertEquals(1, tickets.get(0).getHolders().size());
        assertEquals(3, tickets.get(1).getHolders().size());
        assertTrue(tickets.get(1).admits(((GroupTicket) tickets.get(1)).getMembers().get(2).getUser()));
    }

    @Test
    void dateRules() {
        Ticket ticket = new IndividualTicket("T1", DAY, user(1), new BigDecimal("40"));
        assertTrue(ticket.isActiveOn(DAY));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
    }

    @Test
    void compositionIsImmutable() {
        GroupTicket ticket = groupTicket(2, "40");
        List<IndividualTicket> members = ticket.getMembers();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, members::clear);
    }
}
