package es.upm.etsisi.park.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static User user(int i) {
        return new User("U" + i, "S", "dni" + i, "u" + i + "@x.com", "Clave1!", "6", "1234567890123456");
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "40,50"})
    void discountBrackets(int members, int expected) {
        assertEquals(expected, GroupDiscount.percentFor(members));
    }

    @Test
    void individualTicketCostsBasePrice() {
        IndividualTicket ticket = new IndividualTicket("T1", DAY, user(1), FORTY);

        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(List.of(ticket.getUser()), ticket.getUsers());
    }

    @Test
    void sixMemberGroupAtFortyCosts192() {
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            composition.add(new IndividualTicket("TG:" + i, DAY, user(i), FORTY, GroupDiscount.percentFor(6)));
        }
        GroupTicket ticket = new GroupTicket("TG", DAY, new Group("G", "n"), composition);

        assertEquals(new BigDecimal("32.00"), composition.get(0).getPrice());
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertEquals(6, ticket.getUsers().size());
    }

    @Test
    void groupReferenceCanBeCleared() {
        GroupTicket ticket = new GroupTicket("TG", DAY, new Group("G", "n"),
                List.of(new IndividualTicket("TG:1", DAY, user(1), FORTY)));
        ticket.detachGroup();

        assertNull(ticket.getGroup());
        assertEquals(1, ticket.getTickets().size());
    }

    @Test
    void dateRules() {
        Ticket ticket = new IndividualTicket("T1", DAY, user(1), FORTY);

        assertTrue(ticket.isActiveOn(DAY));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
    }

    @Test
    void attractionEntryIsAllOrNothing() {
        Attraction attraction = new Attraction("A", "n", 2);

        assertFalse(attraction.enter(List.of(user(1), user(2), user(3))));
        assertEquals(0, attraction.getOccupants().size());
        assertTrue(attraction.enter(List.of(user(1), user(2))));
        assertEquals(0, attraction.getAvailablePlaces());
    }
}
