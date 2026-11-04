package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "h", "1", "1234567890123456");
    private final IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));

    @Test
    void speedPassCostsTwentyPercentOfTheTicket() {
        SpeedPass p = new SpeedPass("SP1-1", ticket);
        assertEquals(new BigDecimal("8.00"), p.getPrice());
        assertSame(ticket, p.getTicket());
        assertFalse(p.isConsumed());
        assertEquals("SpeedPass", p.kind());
        assertEquals(new BigDecimal("7.20"), SpeedPass.priceFor(new BigDecimal("36.00")));
        assertEquals(new BigDecimal("2.22"), SpeedPass.priceFor(new BigDecimal("11.12")));
    }

    @Test
    void specialPassCostsTheTicketPrice() {
        SpecialPass p = new SpecialPass("VIP1", ticket);
        assertEquals(new BigDecimal("40.00"), p.getPrice());
        assertEquals("SpecialPass", p.kind());
    }

    @Test
    void consumptionIsRecorded() {
        Pass p = new SpeedPass("SP1-1", ticket);
        p.markConsumed();
        assertTrue(p.isConsumed());
        assertTrue(new SpecialPass("VIP1", new BigDecimal("40.00"), ticket, true).isConsumed());
    }
}
