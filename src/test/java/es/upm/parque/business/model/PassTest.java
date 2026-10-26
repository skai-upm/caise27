package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PassTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "h", "612345678",
            "1234567890123456");
    private final IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana,
            new BigDecimal("60.00"));

    @Test
    void pricesDependOnTheTicketPrice() {
        assertEquals(new BigDecimal("12.00"), SpeedPass.priceFor(ticket));
        assertEquals(new BigDecimal("60.00"), SpecialPass.priceFor(ticket));
    }

    @Test
    void ticketHasManySpeedPassesAndOneSpecialPass() {
        ticket.addSpeedPass(new SpeedPass("T001-SP1", SpeedPass.priceFor(ticket), false));
        ticket.addSpeedPass(new SpeedPass("T001-SP2", SpeedPass.priceFor(ticket), false));
        SpecialPass special = new SpecialPass("T001-SPECIAL", SpecialPass.priceFor(ticket), false);
        ticket.setSpecialPass(special);
        assertThrows(IllegalArgumentException.class,
                () -> ticket.setSpecialPass(new SpecialPass("X", BigDecimal.ONE, false)));
        assertEquals(3, ticket.getPasses().size());
        assertSame(special, ticket.getSpecialPass());
        ticket.removePass(special);
        assertEquals(2, ticket.getPasses().size());
    }

    @Test
    void consumedPassesAreKept() {
        SpecialPass special = new SpecialPass("T001-SPECIAL", SpecialPass.priceFor(ticket), false);
        ticket.setSpecialPass(special);
        assertFalse(special.isConsumed());
        special.consume();
        assertTrue(special.isConsumed());
        assertThrows(IllegalArgumentException.class, special::consume);
        assertTrue(ticket.hasPasses());
        assertThrows(IllegalArgumentException.class,
                () -> ticket.setSpecialPass(new SpecialPass("T001-SPECIAL", BigDecimal.ONE, false)));
    }
}
