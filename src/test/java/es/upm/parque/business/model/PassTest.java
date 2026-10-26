package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.ParkException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "h", "6", "1234567890123456");

    @Test
    void consumedOnceAndKept() {
        SpeedPass pass = new SpeedPass("SP1", new BigDecimal("8.00"), false);
        assertFalse(pass.isConsumed());
        pass.consume();
        assertTrue(pass.isConsumed());
        assertThrows(ParkException.class, pass::consume);
        assertEquals("SpeedPass", pass.getKind());
        assertEquals("Special Pass", new SpecialPass("XP1", BigDecimal.TEN, true).getKind());
    }

    @Test
    void speedPassCostsTwentyPercent() {
        assertEquals(new BigDecimal("8.00"), SpeedPass.priceFor(new BigDecimal("40.00")));
        assertEquals(new BigDecimal("12.00"), SpeedPass.priceFor(new BigDecimal("60.00")));
        assertEquals(new BigDecimal("2.47"), SpeedPass.priceFor(new BigDecimal("12.33")));
    }

    @Test
    void ticketOwnsManySpeedPassesAndOneSpecialPass() {
        IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));
        assertNull(ticket.getSpecialPass());
        SpeedPass a = new SpeedPass("SP1", new BigDecimal("8.00"), false);
        SpeedPass b = new SpeedPass("SP2", new BigDecimal("8.00"), false);
        SpecialPass special = new SpecialPass("XP1", new BigDecimal("40.00"), false);
        ticket.addSpeedPass(a);
        ticket.addSpeedPass(b);
        ticket.setSpecialPass(special);
        assertEquals(List.of(a, b, special), ticket.getPasses());
        assertTrue(ticket.removeSpeedPass(a));
        ticket.setSpecialPass(null);
        assertEquals(List.of(b), ticket.getPasses());
    }
}
