package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");

    private IndividualTicket ticket(String price) {
        return new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal(price));
    }

    @Test
    void speedPassCostsTwentyPercent() {
        assertEquals(new BigDecimal("8.00"), new SpeedPass("T001-SP1", ticket("40.00")).getPrice());
        assertEquals(new BigDecimal("12.00"), new SpeedPass("T001-SP1", ticket("60.00")).getPrice());
        assertEquals(new BigDecimal("2.47"), new SpeedPass("T001-SP1", ticket("12.35")).getPrice());
    }

    @Test
    void specialPassCostsTheTicketPrice() {
        assertEquals(new BigDecimal("40.00"), new SpecialPass("T001-SPECIAL", ticket("40.00")).getPrice());
    }

    @Test
    void consumptionIsKept() {
        Pass pass = new SpeedPass("T001-SP1", ticket("40.00"));
        assertFalse(pass.isConsumed());
        pass.consume();
        assertTrue(pass.isConsumed());
        assertEquals("SPEEDPASS", pass.getType());
        assertEquals("SPECIALPASS", new SpecialPass("T001-SPECIAL", ticket("40.00")).getType());
    }
}
