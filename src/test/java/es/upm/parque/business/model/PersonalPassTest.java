package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.Fixtures;

class PersonalPassTest {

    private final IndividualTicket ticket =
            new IndividualTicket("T001", LocalDate.of(2026, 10, 15), Fixtures.ana(), new BigDecimal("40.00"));

    @Test
    void pricesDependOnTheTicket() {
        assertEquals(new BigDecimal("8.00"), SpeedPass.priceFor(ticket));
        assertEquals(new BigDecimal("40.00"), SpecialPass.priceFor(ticket));
        IndividualTicket odd = new IndividualTicket("T2", LocalDate.of(2026, 10, 15), Fixtures.ana(),
                new BigDecimal("33.33"));
        assertEquals(new BigDecimal("6.67"), SpeedPass.priceFor(odd));
    }

    @Test
    void consumedPassesKeepTheirData() {
        SpeedPass pass = new SpeedPass("T001-S1", new BigDecimal("8.00"), false);
        assertFalse(pass.isConsumed());
        pass.consume();
        assertTrue(pass.isConsumed());
        assertEquals("T001-S1", pass.getId());
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
        assertEquals("SPEED", pass.getKind());
        assertEquals("SPECIAL", new SpecialPass("T001-SPECIAL", BigDecimal.TEN, true).getKind());
    }

    @Test
    void ticketComposesItsPasses() {
        SpeedPass s1 = new SpeedPass("T001-S1", new BigDecimal("8.00"), false);
        SpeedPass s2 = new SpeedPass("T001-S2", new BigDecimal("8.00"), false);
        SpecialPass special = new SpecialPass("T001-SPECIAL", new BigDecimal("40.00"), false);
        assertNull(ticket.getSpecialPass());
        ticket.addSpeedPass(s1);
        ticket.addSpeedPass(s2);
        ticket.setSpecialPass(special);
        assertEquals(List.of(s1, s2, special), ticket.getPasses());
        assertTrue(ticket.removeSpeedPass(s1));
        assertFalse(ticket.removeSpeedPass(s1));
        ticket.setSpecialPass(null);
        assertEquals(List.of(s2), ticket.getPasses());
        List<SpeedPass> view = ticket.getSpeedPasses();
        assertThrows(UnsupportedOperationException.class, () -> view.add(s1));
    }
}
