package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void pricesDeriveFromTheTicketPrice() {
        IndividualTicket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", t).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", t).getPrice());
        IndividualTicket discounted = new IndividualTicket("TG02:1", day, new BigDecimal("40.00"),
                new BigDecimal("0.20"), ana);
        assertEquals(new BigDecimal("6.40"), new SpeedPass("SP02:1", discounted).getPrice());
        assertEquals(new BigDecimal("32.00"), new SpecialPass("PE02", discounted).getPrice());
    }

    @Test
    void consumptionAndRefundability() {
        IndividualTicket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        Pass p = new SpeedPass("SP01:1", t);
        assertEquals("SP01:1", p.getId());
        assertSame(t, p.getTicket());
        assertFalse(p.isConsumed());
        assertTrue(p.isRefundable(day.minusDays(1)));
        assertFalse(p.isRefundable(day));
        p.consume();
        assertTrue(p.isConsumed());
        assertFalse(p.isRefundable(day.minusDays(1)));
    }
}
