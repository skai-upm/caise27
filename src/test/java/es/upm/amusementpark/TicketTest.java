package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void activeOnItsDateAndBeforeHistoricalAfter() {
        Ticket ticket = new IndividualTicket("T001", DAY, ana(), new BigDecimal("40.00"));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertTrue(ticket.isActiveOn(DAY));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
    }

    @Test
    void deletableOnlyBeforeItsDate() {
        Ticket ticket = new IndividualTicket("T001", DAY, ana(), new BigDecimal("40.00"));
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertFalse(ticket.isDeletableOn(DAY.plusDays(1)));
    }

    @Test
    void individualTicketKeepsUserAndPrice() {
        User ana = ana();
        IndividualTicket ticket = new IndividualTicket("T001", DAY, ana, new BigDecimal("45.50"));
        assertEquals(new BigDecimal("45.50"), ticket.getPrice());
        assertTrue(ticket.getUser() == ana);
        assertFalse(ticket.isPartOfGroupTicket());
    }

    @Test
    void repositorySharesIdentifierSpaceAndFindsTicketsByUser() {
        TicketRepository repository = new TicketRepository();
        User ana = ana();
        repository.save(new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00")));
        assertTrue(repository.exists("T001"));
        assertFalse(repository.exists("T002"));
        assertEquals(1, repository.findIndividualTicketsOf(ana).size());
        assertTrue(repository.findIndividualTicketsOf(ana()).isEmpty());
    }
}
