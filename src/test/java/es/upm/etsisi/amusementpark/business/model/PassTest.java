package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("12345678Z", "ana@correo.com");
    private final IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2027, 4, 15), ana,
            new BigDecimal("40.00"));

    @Test
    void pricesFromTheTicketPrice() {
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", ticket).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", ticket).getPrice());
        IndividualTicket discounted = new IndividualTicket("TG02:1", LocalDate.of(2027, 4, 15), ana,
                new BigDecimal("32.00"));
        assertEquals(new BigDecimal("6.40"), new SpeedPass("SP02:1", discounted).getPrice());
    }

    @Test
    void consumptionAndRefundability() {
        Pass pass = new SpeedPass("SP01:1", ticket);
        assertFalse(pass.isConsumed());
        assertTrue(pass.isRefundable(LocalDate.of(2027, 4, 14)));
        assertFalse(pass.isRefundable(LocalDate.of(2027, 4, 15)));
        pass.consume();
        assertTrue(pass.isConsumed());
        assertFalse(pass.isRefundable(LocalDate.of(2027, 4, 14)));
    }
}
