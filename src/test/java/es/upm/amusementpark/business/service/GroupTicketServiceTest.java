package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.GroupOperationException;
import es.upm.amusementpark.business.exception.TicketOperationException;
import es.upm.amusementpark.business.exception.UserOperationException;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Group ticket purchase and deletion, and the locks they impose on groups and
 * users.
 */
class GroupTicketServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 1);
    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private TicketService tickets;
    private GroupService groups;
    private UserService users;

    @BeforeEach
    void setUp() throws Exception {
        UserRepository userRepository = new UserRepository();
        park.initialize(userRepository, new GroupRepository(), TODAY);
        users = UserService.getInstance();
        tickets = TicketService.getInstance();
        groups = GroupService.getInstance();
        users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        users.createUser("Luis", "Perez", "00000000T", "luis@correo.com", "Clave3$", "600000000",
                "1111222233334444");
        groups.createGroup("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    @Test
    void groupTicketIsComposedOfMemberTickets() throws TicketOperationException {
        GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", DAY);
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertTrue(park.getTickets().exists("TG01:12345678Z"));
        assertTrue(park.getTickets().exists("TG01:87654321X"));
        assertEquals("G01", ticket.getGroup().getIdentifier());
        assertEquals(2, ticket.getComposition().size());
    }

    @Test
    void emptyUnknownGroupAndPastDateAreRejected() throws Exception {
        groups.createGroup("G02", "Vacio");
        assertThrows(TicketOperationException.class, () -> tickets.createGroupTicket("TG02", "G02", DAY));
        assertThrows(TicketOperationException.class, () -> tickets.createGroupTicket("TG03", "G99", DAY));
        assertThrows(TicketOperationException.class,
                () -> tickets.createGroupTicket("TG03", "G01", TODAY.minusDays(1)));
        assertTrue(park.getTickets().findAll().isEmpty());
    }

    @Test
    void derivedIdentifierConflictCreatesNothing() throws TicketOperationException {
        tickets.createTicket("TG04:87654321X", "pedro@correo.com", DAY);
        assertThrows(TicketOperationException.class, () -> tickets.createGroupTicket("TG04", "G01", DAY));
        assertFalse(park.getTickets().exists("TG04"));
        assertFalse(park.getTickets().exists("TG04:12345678Z"));
    }

    @Test
    void futureGroupTicketDeletionRemovesCompositionAndUnlocksGroup() throws Exception {
        tickets.createGroupTicket("TG01", "G01", DAY);
        assertThrows(GroupOperationException.class, () -> groups.addUser("G01", "luis@correo.com"));
        assertThrows(TicketOperationException.class, () -> tickets.deleteTicket("TG01:12345678Z"));
        tickets.deleteGroupTicket("TG01");
        assertFalse(park.getTickets().exists("TG01"));
        assertFalse(park.getTickets().exists("TG01:12345678Z"));
        groups.addUser("G01", "luis@correo.com");
        assertEquals(3, park.getGroups().findById("G01").orElseThrow().getMembers().size());
    }

    @Test
    void activeGroupTicketLocksGroupAndCannotBeDeletedOnItsDay() throws Exception {
        tickets.createGroupTicket("TG01", "G01", DAY);
        park.setCurrentDate(DAY);
        assertThrows(TicketOperationException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertThrows(GroupOperationException.class, () -> groups.addUser("G01", "luis@correo.com"));
        assertThrows(GroupOperationException.class, () -> groups.removeUser("G01", "pedro@correo.com"));
        assertThrows(GroupOperationException.class, () -> groups.deleteGroup("G01"));
        assertThrows(UserOperationException.class, () -> users.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(park.getGroups().findById("G01").orElseThrow().contains(
                park.getUsers().findByEmail("ana@correo.com").orElseThrow()));
    }

    @Test
    void historicalGroupTicketsDoNotLockAndSurviveDeletions() throws Exception {
        GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", DAY);
        park.setCurrentDate(DAY.plusDays(1));
        groups.removeUser("G01", "pedro@correo.com");
        assertEquals(2, ticket.getComposition().size());
        users.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.getGroups().findById("G01").orElseThrow().getMembers().isEmpty());
        groups.deleteGroup("G01");
        assertNull(ticket.getGroup());
        assertTrue(park.getTickets().exists("TG01"));
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertEquals("12345678Z", ticket.getComposition().get(0).getUser().getDni());
    }

    @Test
    void lockedGroupMemberCannotBeDeleted() throws Exception {
        tickets.createGroupTicket("TG01", "G01", DAY);
        UserOperationException e = assertThrows(UserOperationException.class,
                () -> users.deleteUser("pedro@correo.com", "Otra2#"));
        assertTrue(e.getMessage().contains("ticket"));
        users.deleteUser("luis@correo.com", "Clave3$");
    }
}
