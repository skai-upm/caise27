package es.upm.park.business.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private static IndividualTicket ticket(String price) {
        return new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal(price));
    }

    @Test
    void pricesComeFromTheTicket() {
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", ticket("40.00")).getPrice());
        assertEquals(new BigDecimal("6.40"), new SpeedPass("SP01:1", ticket("32.00")).getPrice());
        assertEquals(new BigDecimal("9.10"), new SpeedPass("SP01:1", ticket("45.50")).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", ticket("40.00")).getPrice());
    }

    @Test
    void refundableOnlyWhenUnconsumedAndTicketInFuture() {
        Pass pass = new SpeedPass("SP01:1", ticket("40.00"));
        assertTrue(pass.isRefundableOn(DAY.minusDays(1)));
        assertFalse(pass.isRefundableOn(DAY));
        pass.consume();
        assertTrue(pass.isConsumed());
        assertFalse(pass.isRefundableOn(DAY.minusDays(1)));
    }
}
