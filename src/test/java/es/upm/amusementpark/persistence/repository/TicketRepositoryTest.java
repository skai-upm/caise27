package es.upm.amusementpark.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

class TicketRepositoryTest {

    @Test
    void sharedIdentifierSpace() {
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        TicketRepository repo = new TicketRepository();
        Ticket t = new IndividualTicket("T001", LocalDate.of(2027, 4, 15), new BigDecimal("40.00"), ana);
        assertTrue(repo.isIdAvailable("T001"));
        repo.add(t);
        assertFalse(repo.isIdAvailable("T001"));
        assertSame(t, repo.findById("T001"));
        repo.remove(t);
        assertNull(repo.findById("T001"));
    }
}
