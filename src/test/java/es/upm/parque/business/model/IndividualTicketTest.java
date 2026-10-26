package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class IndividualTicketTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));

    @Test
    void currentOrFutureIncludesTheSameDay() {
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 1)));
        assertTrue(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 15)));
        assertFalse(ticket.isCurrentOrFuture(LocalDate.of(2026, 10, 16)));
    }

    @Test
    void futureExcludesTheSameDay() {
        assertTrue(ticket.isFuture(LocalDate.of(2026, 10, 14)));
        assertFalse(ticket.isFuture(LocalDate.of(2026, 10, 15)));
    }
}
