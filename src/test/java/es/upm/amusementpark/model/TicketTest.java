package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");

    @Test
    void individualTicketKeepsItsData() {
        IndividualTicket ticket = new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
        assertEquals("T001", ticket.getId());
        assertEquals(DAY, ticket.getDate());
        assertEquals(ana, ticket.getUser());
        assertEquals(List.of(ana), ticket.getHolders());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(0, ticket.getDiscountPercent());
        assertTrue(ticket.isHeldBy(ana));
    }

    @Test
    void discountIsAppliedAndRoundedHalfUp() {
        assertEquals(new BigDecimal("32.00"), new IndividualTicket("T", DAY, ana, new BigDecimal("40.00"), 20).getPrice());
        assertEquals(new BigDecimal("40.95"), new IndividualTicket("T", DAY, ana, new BigDecimal("45.50"), 10).getPrice());
        assertEquals(new BigDecimal("0.08"), new IndividualTicket("T", DAY, ana, new BigDecimal("0.15"), 50).getPrice());
    }

    @Test
    void statusDependsOnTheCurrentDate() {
        Ticket ticket = new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertTrue(ticket.isActiveOn(DAY));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));

        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.minusDays(1)));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));

        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertFalse(ticket.isDeletableOn(DAY.plusDays(1)));
    }
}
