package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "h", "6", "1234567890123456");
    private final IndividualTicket t001 = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), ana);

    @Test
    void prices() {
        assertEquals(new BigDecimal("8.00"), SpeedPass.priceFor(t001));
        assertEquals(new BigDecimal("40.00"), SpecialPass.priceFor(t001));
        IndividualTicket odd = new IndividualTicket("T2", t001.getDate(), new BigDecimal("33.33"), ana);
        assertEquals(new BigDecimal("6.67"), SpeedPass.priceFor(odd));
    }

    @Test
    void stateAndConsumption() {
        Pass p = new SpeedPass("SP01-1", new BigDecimal("8.00"), t001, false);
        assertEquals("SP01-1", p.getId());
        assertSame(t001, p.getTicket());
        assertFalse(p.isConsumed());
        p.consume();
        assertTrue(p.isConsumed());
        assertEquals("SPEED", p.kind());
        assertEquals("SPECIAL", new SpecialPass("S", BigDecimal.ONE, t001, false).kind());
    }
}
