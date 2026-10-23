package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PassTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "", "6", "1234567890123456");

    private IndividualTicket ticket(String base, int discount) {
        return new IndividualTicket("T001", DAY, ana, new BigDecimal(base), discount);
    }

    @Test
    void pricesComeFromTheTicketsStoredPrice() {
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", ticket("40.00", 0)).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", ticket("40.00", 0)).getPrice());
        assertEquals(new BigDecimal("7.20"), new SpeedPass("SP02:1", ticket("40.00", 10)).getPrice());
        assertEquals(new BigDecimal("36.00"), new SpecialPass("PE02", ticket("40.00", 10)).getPrice());
        assertEquals(new BigDecimal("9.10"), new SpeedPass("SP03:1", ticket("45.50", 0)).getPrice());
    }

    @Test
    void consumptionIsRecordedAndCanBeUndone() {
        Pass pass = new SpeedPass("SP01:1", ticket("40.00", 0));
        assertFalse(pass.isConsumed());
        pass.consume();
        assertTrue(pass.isConsumed());
        assertThrows(IllegalStateException.class, pass::consume);
        pass.revertConsumption();
        assertFalse(pass.isConsumed());
    }

    @Test
    void refundableOnlyWhenUnconsumedAndTicketIsInTheFuture() {
        Pass pass = new SpecialPass("PE01", ticket("40.00", 0));
        assertTrue(pass.isRefundableOn(DAY.minusDays(1)));
        assertFalse(pass.isRefundableOn(DAY));
        pass.consume();
        assertFalse(pass.isRefundableOn(DAY.minusDays(1)));
    }

    @Test
    void passesKnowTheirTypeAndTicket() {
        IndividualTicket ticket = ticket("40.00", 0);
        assertEquals(PassType.SPEED, new SpeedPass("S", ticket).getType());
        assertEquals(PassType.SPECIAL, new SpecialPass("P", ticket).getType());
        assertTrue(new SpecialPass("P", ticket).getTicket() == ticket);
    }
}
