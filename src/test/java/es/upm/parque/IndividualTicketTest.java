package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class IndividualTicketTest {

    private final User ana = UserManagerTest.ana();
    private final IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));

    @Test
    void holdsItsData() {
        assertEquals("T001", ticket.getId());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
        assertSame(ana, ticket.getHolder());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(List.of(ana), ticket.getVisitors());
    }

    @Test
    void currentOnOrAfterToday() {
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 1)));
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 15)));
        assertFalse(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 16)));
    }

    @Test
    void refundableOnlyStrictlyBeforeItsDate() {
        assertTrue(ticket.isRefundable(LocalDate.of(2026, 10, 14)));
        assertFalse(ticket.isRefundable(LocalDate.of(2026, 10, 15)));
        assertFalse(ticket.isRefundable(LocalDate.of(2026, 10, 16)));
    }
}
