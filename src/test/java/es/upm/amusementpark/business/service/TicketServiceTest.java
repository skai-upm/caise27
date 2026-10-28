package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

class TicketServiceTest {

    private LocalDate today = LocalDate.of(2027, 4, 15);
    private BigDecimal price = new BigDecimal("40.00");
    private final UserService users = new UserService(new UserRepository(), new InMemoryUserStorage());
    private final TicketService tickets = new TicketService(new TicketRepository(), users, () -> today, () -> price);

    @BeforeEach
    void users() throws ParkException {
        users.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private void assertRejected(String reason, Executable op) {
        ParkException e = assertThrows(ParkException.class, op);
        assertTrue(e.getMessage().contains(reason), e.getMessage());
    }

    @Test
    void createUsesCurrentPriceAndKeepsIt() throws ParkException {
        IndividualTicket t1 = tickets.createIndividualTicket("T001", "ana@correo.com", today);
        price = new BigDecimal("45.50");
        IndividualTicket t2 = tickets.createIndividualTicket("T002", "ana@correo.com", today.plusDays(1));
        price = new BigDecimal("50.00");
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals("12345678Z", t1.getUser().getDni());
    }

    @Test
    void creationRules() throws ParkException {
        tickets.createIndividualTicket("T001", "ana@correo.com", today);
        assertRejected("already exists", () -> tickets.createIndividualTicket("T001", "ana@correo.com", today));
        assertRejected("User not found", () -> tickets.createIndividualTicket("T002", "x@correo.com", today));
        assertRejected("past dates", () -> tickets.createIndividualTicket("T003", "ana@correo.com", today.minusDays(1)));
        assertRejected("required", () -> tickets.createIndividualTicket("", "ana@correo.com", today));
        assertEquals(1, tickets.findAll().size());
    }

    @Test
    void onlyStrictlyFutureTicketsCanBeDeleted() throws ParkException {
        tickets.createIndividualTicket("T001", "ana@correo.com", today);
        tickets.createIndividualTicket("T002", "ana@correo.com", today.plusDays(1));
        assertRejected("after the current date", () -> tickets.deleteIndividualTicket("T001"));
        tickets.deleteIndividualTicket("T002");
        assertTrue(tickets.findById("T002").isEmpty());
        assertRejected("not found", () -> tickets.deleteIndividualTicket("T999"));
        today = today.plusDays(1);
        assertRejected("after the current date", () -> tickets.deleteIndividualTicket("T001"));
    }

    @Test
    void activeTicketsAreDerivedFromTicketsAndDate() throws ParkException {
        assertFalse(tickets.hasActiveTicket("12345678Z"));
        tickets.createIndividualTicket("T001", "ana@correo.com", today);
        assertTrue(tickets.hasActiveTicket("12345678Z"));
        today = today.plusDays(1);
        assertFalse(tickets.hasActiveTicket("12345678Z"));
    }

    @Test
    void ticketSeesUserUpdates() throws ParkException {
        IndividualTicket t = tickets.createIndividualTicket("T001", "ana@correo.com", today);
        users.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana2@correo.com");
        assertEquals("ana2@correo.com", t.getUser().getEmail());
    }
}
