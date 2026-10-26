package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassTest {

    private IndividualTicket ticket;

    @BeforeEach
    void setUp() {
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "hash", "612345678",
            "1234567890123456");
        ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), ana);
    }

    @Test
    void speedPassesAreIndependentAndPricedAtTwentyPercent() {
        List<SpeedPass> pack = ticket.addSpeedPasses(10);
        assertEquals(10, pack.size());
        assertEquals("T001-S1", pack.get(0).getId());
        assertEquals("T001-S10", pack.get(9).getId());
        for (SpeedPass pass : pack) {
            assertEquals(new BigDecimal("8.00"), pass.getPrice());
            assertEquals(PassStatus.AVAILABLE, pass.getStatus());
        }
        pack.get(0).consume();
        assertEquals(PassStatus.AVAILABLE, pack.get(1).getStatus());
        assertEquals("T001-S11", ticket.addSpeedPasses(1).get(0).getId());
        assertEquals(11, ticket.getSpeedPasses().size());
    }

    @Test
    void speedPassPriceIsRounded() {
        IndividualTicket odd = new IndividualTicket("T002", LocalDate.of(2026, 10, 15), new BigDecimal("33.33"),
            new User("P", "L", "87654321X", "p@correo.com", "h", "6", "1234567890123456"));
        assertEquals(new BigDecimal("6.67"), odd.getSpeedPassPrice());
    }

    @Test
    void oneSpecialPassPerLifetime() {
        assertNull(ticket.getSpecialPass());
        SpecialPass special = ticket.addSpecialPass();
        assertEquals("T001-SPECIAL", special.getId());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        special.refund();
        assertEquals(PassStatus.REFUNDED, special.getStatus());
        assertThrows(IllegalArgumentException.class, () -> ticket.addSpecialPass());
        assertSame(special, ticket.getSpecialPass());
    }

    @Test
    void consumedAndRefundedPassesStayWithTheirStatus() {
        SpeedPass pass = ticket.addSpeedPasses(1).get(0);
        pass.consume();
        assertEquals(PassStatus.CONSUMED, pass.getStatus());
        assertThrows(IllegalArgumentException.class, pass::refund);
        assertThrows(IllegalArgumentException.class, pass::consume);
        assertTrue(ticket.getPasses().contains(pass));
    }

    @Test
    void restoredPassesAreAttached() {
        ticket.restorePass(new SpeedPass("T001-S1", new BigDecimal("8.00"), PassStatus.CONSUMED));
        ticket.restorePass(new SpecialPass("T001-SPECIAL", new BigDecimal("40.00"), PassStatus.REFUNDED));
        assertEquals(2, ticket.getPasses().size());
        assertEquals("T001-S2", ticket.addSpeedPasses(1).get(0).getId());
        assertThrows(IllegalArgumentException.class, () -> ticket.addSpecialPass());
        assertEquals("SpeedPass", ticket.getPasses().get(0).getTypeName());
        assertEquals("SpecialPass", ticket.getPasses().get(2).getTypeName());
    }
}
