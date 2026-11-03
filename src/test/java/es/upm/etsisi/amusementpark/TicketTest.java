package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void ticketOfRegisteredUser() {
        User ana = GroupTest.user("12345678Z", "ana@correo.com");
        Ticket t = new Ticket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40"));
        assertEquals("T001", t.getId());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals("40.00", t.getPrice().toPlainString());
        assertSame(ana, t.getUser().orElseThrow());
        assertEquals("12345678Z", t.getOwnerDni());
        assertTrue(t.belongsTo(ana));
        assertFalse(t.belongsTo(GroupTest.user("12345678Z", "ana@correo.com")));
    }

    @Test
    void historicalTicketWithoutUser() {
        Ticket t = new Ticket("T001", "12345678Z", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"));
        assertTrue(t.getUser().isEmpty());
        assertEquals("12345678Z", t.getOwnerDni());
    }
}
