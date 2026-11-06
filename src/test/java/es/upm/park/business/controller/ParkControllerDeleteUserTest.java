package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.service.ParkException;

class ParkControllerDeleteUserTest {

    private final ParkController park = ParkController.getInstance();
    private UserRepository users;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        park.start(users, LocalDate.of(2027, 4, 15));
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private void assertRejected(String message) {
        ParkException e = assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertEquals(message, e.getMessage());
        assertTrue(users.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void activeTicketBlocksUntilFutureTicketDeleted() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", LocalDate.of(2027, 4, 16));
        assertRejected("user ana@correo.com has an active ticket");
        park.getTicketService().deleteIndividual("T001");
        park.deleteUser("ana@correo.com", "Clave1!");
        assertFalse(users.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void ticketForTodayKeepsBlocking() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", LocalDate.of(2027, 4, 15));
        assertRejected("user ana@correo.com has an active ticket");
    }

    @Test
    void historicalTicketDoesNotBlockAndIsKept() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", LocalDate.of(2027, 4, 15));
        park.setCurrentDate("2027-04-16");
        park.deleteUser("ana@correo.com", "Clave1!");
        IndividualTicket kept = (IndividualTicket) park.getTicketService().find("T001");
        assertEquals("12345678Z", kept.getUser().getDni());
    }

    @Test
    void deletedUserLeavesModifiableGroups() {
        park.getGroupService().create("G01", "A");
        park.getGroupService().create("G02", "B");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G02", "ana@correo.com");
        assertSame(users.findByDni("12345678Z").orElseThrow(), park.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(park.getGroupService().find("G01").getMemberDnis().isEmpty());
        assertTrue(park.getGroupService().find("G02").getMemberDnis().isEmpty());
    }

    @Test
    void wrongPasswordStillRejected() {
        ParkException e = assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "x"));
        assertEquals("incorrect password", e.getMessage());
    }
}
