package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void individualTicketHoldsUserAndPrice() {
        Ticket ticket = new IndividualTicket("T001", TODAY, ana, new BigDecimal("40.00"));
        assertEquals("T001", ticket.getId());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertSame(ana, ((IndividualTicket) ticket).getUser());
        assertEquals(List.of(ticket), ticket.individualTickets());
        assertFalse(((IndividualTicket) ticket).isPartOfGroupTicket());
    }

    @Test
    void activeAndDeletableBoundaries() {
        Ticket today = new IndividualTicket("A", TODAY, ana, BigDecimal.ONE);
        Ticket tomorrow = new IndividualTicket("B", TODAY.plusDays(1), ana, BigDecimal.ONE);
        Ticket yesterday = new IndividualTicket("C", TODAY.minusDays(1), ana, BigDecimal.ONE);
        assertTrue(today.isActiveOn(TODAY));
        assertTrue(tomorrow.isActiveOn(TODAY));
        assertFalse(yesterday.isActiveOn(TODAY));
        assertFalse(today.isDeletableOn(TODAY));
        assertTrue(tomorrow.isDeletableOn(TODAY));
        assertFalse(yesterday.isDeletableOn(TODAY));
    }
}
