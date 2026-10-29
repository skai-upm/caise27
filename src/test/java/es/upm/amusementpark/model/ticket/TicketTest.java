package es.upm.amusementpark.model.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");

    @Test
    void commonAbstraction() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }

    @Test
    void activeDeletableAndValidRules() {
        Ticket ticket = new IndividualTicket("T001", DAY, ana, FORTY, 0);

        assertTrue(ticket.isActive(DAY.minusDays(1)));
        assertTrue(ticket.isActive(DAY));
        assertFalse(ticket.isActive(DAY.plusDays(1)));

        assertTrue(ticket.isDeletable(DAY.minusDays(1)));
        assertFalse(ticket.isDeletable(DAY));
        assertFalse(ticket.isDeletable(DAY.plusDays(1)));

        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
    }

    @Test
    void individualTicketPriceAndUser() {
        IndividualTicket ticket = new IndividualTicket("T001", DAY, ana, FORTY, 0);
        assertEquals(FORTY, ticket.getPrice());
        assertEquals(List.of(ana), ticket.getUsers());
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "40,50"})
    void discountBrackets(int members, int expected) {
        assertEquals(expected, GroupTicket.discountFor(members));
    }

    @ParameterizedTest
    @CsvSource({"2,40.00,80.00", "3,36.00,108.00", "5,36.00,180.00", "6,32.00,192.00", "10,32.00,320.00",
        "11,20.00,220.00"})
    void groupPriceIsSumOfDiscountedTickets(int members, String perTicket, String total) {
        int discount = GroupTicket.discountFor(members);
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            composition.add(new IndividualTicket("TG:" + i, DAY, ana, FORTY, discount));
        }
        GroupTicket groupTicket = new GroupTicket("TG", DAY, new Group("G", "g"), composition);

        assertEquals(new BigDecimal(perTicket), composition.get(0).getPrice());
        assertEquals(new BigDecimal(total), groupTicket.getPrice());
        assertEquals(members, groupTicket.getUsers().size());
    }

    @Test
    void compositionIsFixedAndGroupCanBeDetached() {
        List<IndividualTicket> composition = new ArrayList<>();
        composition.add(new IndividualTicket("TG:1", DAY, ana, FORTY, 0));
        GroupTicket groupTicket = new GroupTicket("TG", DAY, new Group("G", "g"), composition);
        composition.clear();

        assertEquals(1, groupTicket.getComposition().size());
        assertThrows(UnsupportedOperationException.class, () -> groupTicket.getComposition().clear());
        groupTicket.detachGroup();
        assertNull(groupTicket.getGroup());
    }

    @Test
    void discountRoundsToCents() {
        IndividualTicket ticket = new IndividualTicket("T", DAY, ana, new BigDecimal("45.55"), 10);
        assertEquals(new BigDecimal("41.00"), ticket.getPrice());
    }
}
