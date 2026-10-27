package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "x", "1",
            "1234567890123456");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void pricesDeriveFromTicketPrice() {
        IndividualTicket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        assertEquals(new BigDecimal("8.00"), new SpeedPass("SP01:1", t).getPrice());
        assertEquals(new BigDecimal("40.00"), new SpecialPass("PE01", t).getPrice());
    }

    @Test
    void discountedComponentPrices() {
        User p = new User("P", "L", "87654321X", "p@correo.com", "x", "1", "1");
        User l = new User("L", "R", "11111111H", "l@correo.com", "x", "1", "1");
        GroupTicket g = new GroupTicket("TG01", day, new BigDecimal("40.00"), new Group("G01", "g"), List.of(ana, p, l));
        IndividualTicket component = g.getComposition().get(0);
        assertEquals(new BigDecimal("7.20"), new SpeedPass("SP:1", component).getPrice());
        assertEquals(new BigDecimal("36.00"), new SpecialPass("PE", component).getPrice());
    }

    @Test
    void consumptionAndRefundability() {
        IndividualTicket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        Pass pass = new SpeedPass("SP01:1", t);
        assertSame(t, pass.getTicket());
        assertFalse(pass.isConsumed());
        assertTrue(pass.isRefundable(day.minusDays(1)));
        assertFalse(pass.isRefundable(day));
        pass.consume();
        assertTrue(pass.isConsumed());
        assertFalse(pass.isRefundable(day.minusDays(1)));
    }
}
