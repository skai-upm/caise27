package es.upm.etsisi.amusementpark.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");

    @Test
    void individualTicketUsesBasePrice() {
        Ticket t = new IndividualTicket("T001", DAY, ana, FORTY);
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(List.of(ana), t.getHolders());
        assertEquals(List.of(t), t.getIndividualTickets());
    }

    @Test
    void dateRules() {
        Ticket t = new IndividualTicket("T001", DAY, ana, FORTY);
        assertTrue(t.isActiveOn(DAY));
        assertTrue(t.isActiveOn(DAY.minusDays(1)));
        assertFalse(t.isActiveOn(DAY.plusDays(1)));
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.minusDays(1)));
        assertFalse(t.isDeletableOn(DAY));
        assertTrue(t.isDeletableOn(DAY.minusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "30,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @ParameterizedTest
    @CsvSource({"2,40.00,80.00", "3,36.00,108.00", "5,36.00,180.00", "6,32.00,192.00", "10,32.00,320.00", "11,20.00,220.00"})
    void groupTicketPrice(int members, String unit, String total) {
        List<IndividualTicket> composition = new ArrayList<>();
        int discount = GroupTicket.discountFor(members);
        for (int i = 0; i < members; i++) {
            User u = new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "", "");
            composition.add(new IndividualTicket("TG:dni" + i, DAY, u, FORTY, discount));
        }
        GroupTicket g = new GroupTicket("TG", DAY, new Group("G", "n"), composition);
        assertEquals(new BigDecimal(unit), composition.get(0).getPrice());
        assertEquals(new BigDecimal(total), g.getPrice());
        assertEquals(members, g.getHolders().size());
    }

    @Test
    void compositionIsFixedAndGroupCanBeDetached() {
        List<IndividualTicket> composition = new ArrayList<>(List.of(new IndividualTicket("TG:1", DAY, ana, FORTY)));
        GroupTicket g = new GroupTicket("TG", DAY, new Group("G", "n"), composition);
        composition.clear();
        assertEquals(1, g.getIndividualTickets().size());
        assertThrows(UnsupportedOperationException.class, () -> g.getIndividualTickets().clear());
        g.detachGroup();
        assertNull(g.getGroup());
    }

    @Test
    void memberTicketId() {
        assertEquals("TG01:12345678Z", GroupTicket.memberTicketId("TG01", ana));
    }

    @Test
    void attractionEntryIsAllOrNothing() {
        User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "", "");
        Attraction a = new Attraction("A02", "Torre", 1);
        assertThrows(IllegalArgumentException.class, () -> a.enter(List.of(ana, pedro)));
        assertTrue(a.getOccupants().isEmpty());
        a.enter(List.of(ana));
        assertThrows(IllegalArgumentException.class, () -> a.enter(List.of(ana)));
        assertEquals(0, a.getAvailablePlaces());
        assertTrue(a.exit(ana));
        assertEquals(1, a.getAvailablePlaces());
    }
}
