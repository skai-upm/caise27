package es.upm.etsisi.park.model;

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

class TicketModelTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static List<User> users(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(new User("U" + i, "S", String.format("%08dX", i), "u" + i + "@correo.com", "Aa1!", "1",
                    "1234567890123456"));
        }
        return users;
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "20,50"})
    void discountBrackets(int members, int percent) {
        assertEquals(percent, GroupDiscount.forMembers(members).getPercent());
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "6,192.00", "11,220.00"})
    void groupTicketTotals(int members, String total) {
        GroupTicket ticket = new GroupTicket("TG", DAY, FORTY, new Group("G", "g"), users(members));
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getTickets().size());
        assertEquals(members, ticket.getHolders().size());
    }

    @Test
    void sixMembersPayThirtyTwoEach() {
        GroupTicket ticket = new GroupTicket("TG02", DAY, FORTY, null, users(6));
        for (IndividualTicket component : ticket.getTickets()) {
            assertEquals(new BigDecimal("32.00"), component.getPrice());
            assertEquals(20, component.getDiscountPercent());
        }
    }

    @Test
    void identifiersAndLookup() {
        List<User> members = users(2);
        GroupTicket ticket = new GroupTicket("TG01", DAY, FORTY, null, members);
        assertEquals(List.of("TG01", "TG01:00000000X", "TG01:00000001X"), ticket.getIdentifiers());
        assertTrue(ticket.findTicket("TG01").isPresent());
        assertTrue(ticket.findTicket("TG01:00000001X").isPresent());
        assertFalse(ticket.findTicket("TG02").isPresent());
        assertTrue(ticket.contains(ticket.getTickets().get(0)));
        assertTrue(ticket.involves(members.get(1)));
    }

    @Test
    void dateRules() {
        IndividualTicket ticket = new IndividualTicket("T001", DAY, FORTY, users(1).get(0));
        assertTrue(ticket.isActiveOn(DAY));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertEquals(FORTY, ticket.getPrice());
        assertEquals(List.of("T001"), ticket.getIdentifiers());
    }

    @Test
    void groupTicketReferenceCanBeCleared() {
        Group group = new Group("G01", "Familia");
        GroupTicket ticket = new GroupTicket("TG01", DAY, FORTY, group, users(1));
        assertEquals(group, ticket.getGroup());
        ticket.detachGroup();
        assertEquals(null, ticket.getGroup());
        assertEquals(1, ticket.getTickets().size());
    }

    @Test
    void attractionAdmitsAllOrNothing() {
        Attraction attraction = new Attraction("A01", "Dragon Rojo", 3);
        List<User> four = users(4);
        assertFalse(attraction.admitAll(four));
        assertEquals(0, attraction.getOccupancy());
        assertTrue(attraction.admitAll(four.subList(0, 2)));
        assertFalse(attraction.admitAll(four.subList(1, 3)));
        assertEquals(2, attraction.getOccupancy());
        assertEquals(1, attraction.getAvailablePlaces());
        assertTrue(attraction.findOccupant("u0@correo.com").isPresent());
        assertTrue(attraction.release(four.get(0)));
        assertFalse(attraction.release(four.get(0)));
    }

    @Test
    void groupMembersAreUnique() {
        Group group = new Group("G01", "Familia");
        User user = users(1).get(0);
        assertTrue(group.isEmpty());
        assertTrue(group.add(user));
        assertFalse(group.add(user));
        assertTrue(group.remove(user));
        assertFalse(group.remove(user));
    }
}
