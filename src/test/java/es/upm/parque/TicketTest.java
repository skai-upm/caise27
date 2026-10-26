package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TicketTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    private final Ticket ticket = new Ticket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));

    @Test
    void currentOrFutureIncludesTheTicketDay() {
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 14)));
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 15)));
        assertFalse(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 16)));
    }

    @Test
    void futureExcludesTheTicketDay() {
        assertTrue(ticket.isFuture(LocalDate.of(2026, 10, 14)));
        assertFalse(ticket.isFuture(LocalDate.of(2026, 10, 15)));
        assertFalse(ticket.isFuture(LocalDate.of(2026, 10, 16)));
    }
}
