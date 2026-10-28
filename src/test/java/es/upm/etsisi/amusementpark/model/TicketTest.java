package es.upm.etsisi.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TicketTest {

    private final User ana = new User("ana@correo.com");
    private final Ticket ticket = new IndividualTicket("T001", LocalDate.of(2027, 4, 15), ana, new BigDecimal("40.00"));

    @Test
    void activeOnAndBeforeItsDate() {
        assertTrue(ticket.isActive(LocalDate.of(2027, 4, 14)));
        assertTrue(ticket.isActive(LocalDate.of(2027, 4, 15)));
        assertFalse(ticket.isActive(LocalDate.of(2027, 4, 16)));
    }

    @Test
    void deletableOnlyStrictlyBeforeItsDate() {
        assertTrue(ticket.isDeletable(LocalDate.of(2027, 4, 14)));
        assertFalse(ticket.isDeletable(LocalDate.of(2027, 4, 15)));
        assertFalse(ticket.isDeletable(LocalDate.of(2027, 4, 16)));
    }

    @Test
    void commonAbstractionExposesData() {
        assertEquals("T001", ticket.getIdentifier());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(1, ticket.getIndividualTickets().size());
        assertSame(ana, ticket.getIndividualTickets().get(0).getUser());
    }
}
