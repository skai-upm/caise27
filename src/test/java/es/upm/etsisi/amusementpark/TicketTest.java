package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TicketTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");

    @Test
    void holdsIdDateUserAndPrice() {
        Ticket t = new Ticket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));
        assertEquals("T001", t.getId());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals("12345678Z", t.getOwnerDni());
        assertSame(ana, t.getUser().orElseThrow());
        assertTrue(t.belongsTo(ana));
        assertFalse(t.belongsTo(new User(ana)));
    }

    @Test
    void detachKeepsDniAsHistory() {
        Ticket t = new Ticket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));
        t.detachUser();
        assertTrue(t.getUser().isEmpty());
        assertFalse(t.belongsTo(ana));
        assertEquals("12345678Z", t.getOwnerDni());
    }
}
