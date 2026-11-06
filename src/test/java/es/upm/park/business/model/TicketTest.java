package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class TicketTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "p@correo.com", "Otra2#", "6", "9876543210987654");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void commonAbstractionExposesSharedData() {
        Ticket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        assertEquals("T001", t.getId());
        assertEquals(day, t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(List.of("T001"), t.ids());
        assertTrue(t.involves(ana));
        assertFalse(t.involves(pedro));
        assertSame(ana, ((IndividualTicket) t).getUser());
    }

    @Test
    void statusDerivedFromDate() {
        Ticket t = new IndividualTicket("T001", day, BigDecimal.TEN, ana);
        assertTrue(t.isActive(day));
        assertFalse(t.isDeletable(day));
        assertTrue(t.isActive(day.minusDays(1)));
        assertTrue(t.isDeletable(day.minusDays(1)));
        assertFalse(t.isActive(day.plusDays(1)));
        assertFalse(t.isDeletable(day.plusDays(1)));
    }
}
