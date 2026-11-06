package es.upm.park.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.model.IndividualTicket;
import es.upm.park.model.User;
import es.upm.park.repository.TicketRepository;
import es.upm.park.repository.UserRepository;

class TicketServiceTest {

    private LocalDate today = LocalDate.of(2027, 4, 1);
    private BigDecimal price = new BigDecimal("40.00");
    private TicketRepository tickets;
    private TicketService service;
    private User ana;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository();
        ana = new UserService(users).create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        tickets = new TicketRepository();
        service = new TicketService(tickets, users, () -> today, () -> price);
    }

    private void assertRejected(String message, Runnable action) {
        int before = tickets.findAll().size();
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
        assertEquals(before, tickets.findAll().size());
    }

    @Test
    void createsAtCurrentPriceAndKeepsItAfterPriceChange() {
        IndividualTicket t = service.createIndividual("T001", "ana@correo.com", LocalDate.of(2027, 4, 15));
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        price = new BigDecimal("45.50");
        IndividualTicket t2 = service.createIndividual("T002", "ana@correo.com", today);
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(ana, t.getUser());
    }

    @Test
    void rejectsInvalidCreations() {
        service.createIndividual("T001", "ana@correo.com", today);
        assertRejected("ticket T001 already exists", () -> service.createIndividual("T001", "ana@correo.com", today));
        assertRejected("no user with email nadie@correo.com",
                () -> service.createIndividual("T002", "nadie@correo.com", today));
        assertRejected("ticket date 2027-03-31 is before the current date 2027-04-01",
                () -> service.createIndividual("T003", "ana@correo.com", today.minusDays(1)));
        assertRejected("ticket identifier is required", () -> service.createIndividual(" ", "ana@correo.com", today));
    }

    @Test
    void deletesOnlyFutureTickets() {
        service.createIndividual("T001", "ana@correo.com", today.plusDays(1));
        service.createIndividual("T002", "ana@correo.com", today);
        assertRejected("ticket T002 cannot be deleted: it is not dated after 2027-04-01",
                () -> service.deleteIndividual("T002"));
        service.deleteIndividual("T001");
        assertFalse(tickets.existsId("T001"));
        assertRejected("no ticket T001", () -> service.deleteIndividual("T001"));
        today = today.plusDays(5);
        assertRejected("ticket T002 cannot be deleted: it is not dated after 2027-04-06",
                () -> service.deleteIndividual("T002"));
    }

    @Test
    void activeTicketDependsOnCurrentDate() {
        assertFalse(service.hasActiveTicket(ana));
        service.createIndividual("T001", "ana@correo.com", today);
        assertTrue(service.hasActiveTicket(ana));
        today = today.plusDays(1);
        assertFalse(service.hasActiveTicket(ana));
    }
}
