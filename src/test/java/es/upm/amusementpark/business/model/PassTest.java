package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "x", "612345678",
            "1234567890123456");

    private IndividualTicket ticket(String price) {
        return new IndividualTicket("T001", LocalDate.of(2027, 4, 15), ana, new BigDecimal(price));
    }

    @Test
    void pricesDeriveFromTheTicketPrice() {
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", ticket("40.00")).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", ticket("40.00")).getPrice());
        assertEquals(new BigDecimal("6.40"), new SpeedPass("SP01:1", ticket("32.00")).getPrice());
        assertEquals(new BigDecimal("32.00"), new SpecialPass("PE01", ticket("32.00")).getPrice());
        assertEquals(new BigDecimal("9.10"), new SpeedPass("SP01:1", ticket("45.50")).getPrice());
    }

    @Test
    void passStartsUnconsumedAndKnowsItsTicket() {
        IndividualTicket t001 = ticket("40.00");
        Pass pass = new SpeedPass("SP01:1", t001);
        assertFalse(pass.isConsumed());
        assertSame(t001, pass.getTicket());
        pass.consume();
        assertTrue(pass.isConsumed());
    }
}
