package es.upm.park.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final Ticket ticket = new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00"));

    @Test
    void activeOnItsDateAndBefore() {
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertTrue(ticket.isActiveOn(DAY));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
    }

    @Test
    void deletableOnlyStrictlyBeforeItsDate() {
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertFalse(ticket.isDeletableOn(DAY.plusDays(1)));
    }

    @Test
    void validOnlyOnItsDate() {
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.minusDays(1)));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
    }

    @Test
    void individualTicketAdmitsItsOwner() {
        assertEquals(java.util.List.of("ana@correo.com"), ticket.admittedUsers());
    }

    @Test
    void keepsCommonData() {
        assertEquals("T001", ticket.getId());
        assertEquals(DAY, ticket.getDate());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals("ana@correo.com", ((IndividualTicket) ticket).getOwnerEmail());
    }
}
