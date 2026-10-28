package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerGroupTicketTest {

    private final ParkController controller = ParkController.getInstance();
    private Group group;

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.addUser(new User("87654321X", "pedro@correo.com"));
        controller.addUser(new User("11111111H", "luis@correo.com"));
        group = controller.createGroup("G01", "Familia Garcia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.addUserToGroup("G01", "pedro@correo.com");
    }

    @Test
    void createGroupTicket() {
        GroupTicket ticket = controller.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertTrue(controller.isTicketIdentifierInUse("TG01:12345678Z"));
        assertTrue(controller.isTicketIdentifierInUse("TG01:87654321X"));
        assertEquals(1, controller.findTicketsOfUser(controller.findUser("ana@correo.com").orElseThrow()).size());
    }

    @Test
    void creationRejections() {
        controller.createGroup("G02", "Vacio");
        assertThrows(ParkException.class, () -> controller.createGroupTicket("TG02", "G02", "2027-04-15"));
        assertThrows(ParkException.class, () -> controller.createGroupTicket("TG03", "G99", "2027-04-15"));
        assertThrows(ParkException.class, () -> controller.createGroupTicket("TG04", "G01", "2027-03-31"));
        assertFalse(controller.isTicketIdentifierInUse("TG04"));
    }

    @Test
    void derivedIdentifierConflictPreventsPartialCreation() {
        controller.createIndividualTicket("TG01:12345678Z", "ana@correo.com", "2027-04-15");
        assertThrows(ParkException.class, () -> controller.createGroupTicket("TG01", "G01", "2027-04-15"));
        assertFalse(controller.isTicketIdentifierInUse("TG01"));
        assertFalse(controller.isTicketIdentifierInUse("TG01:87654321X"));
    }

    @Test
    void groupTicketIdentifierConflictsWithIndividualTicket() {
        controller.createIndividualTicket("T001", "luis@correo.com", "2027-04-15");
        assertThrows(ParkException.class, () -> controller.createGroupTicket("T001", "G01", "2027-04-15"));
    }

    @Test
    void activeGroupTicketLocksGroupUntilHistoricalOrDeleted() {
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        assertFalse(controller.isGroupModifiable(group));
        assertThrows(ParkException.class, () -> controller.addUserToGroup("G01", "luis@correo.com"));
        assertThrows(ParkException.class, () -> controller.removeUserFromGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> controller.deleteGroup("G01"));
        controller.deleteGroupTicket("TG01");
        assertFalse(controller.isTicketIdentifierInUse("TG01:12345678Z"));
        assertTrue(controller.isGroupModifiable(group));
        controller.createGroupTicket("TG02", "G01", "2027-04-15");
        controller.setCurrentDate("2027-04-16");
        assertTrue(controller.isGroupModifiable(group));
        controller.addUserToGroup("G01", "luis@correo.com");
        assertEquals(2, controller.findTicket("TG02").orElseThrow().getIndividualTickets().size());
    }

    @Test
    void groupTicketDeletionDateRules() {
        controller.createGroupTicket("TG01", "G01", "2027-04-01");
        assertThrows(ParkException.class, () -> controller.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> controller.deleteGroupTicket("NOPE"));
        controller.createIndividualTicket("T001", "luis@correo.com", "2027-04-15");
        assertThrows(ParkException.class, () -> controller.deleteGroupTicket("T001"));
        assertTrue(controller.isTicketIdentifierInUse("TG01"));
    }

    @Test
    void composedTicketCannotBeDeletedIndividually() {
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(ParkException.class, () -> controller.deleteIndividualTicket("TG01:12345678Z"));
        assertTrue(controller.isTicketIdentifierInUse("TG01:12345678Z"));
    }

    @Test
    void groupDeletionKeepsHistoricalTickets() {
        GroupTicket ticket = controller.createGroupTicket("TG01", "G01", "2027-04-01");
        assertThrows(ParkException.class, () -> controller.deleteGroup("G01"));
        controller.setCurrentDate("2027-04-02");
        controller.deleteGroup("G01");
        assertTrue(controller.findGroup("G01").isEmpty());
        assertNull(ticket.getGroup());
        assertEquals("TG01", controller.findTicket("TG01").orElseThrow().getIdentifier());
        assertEquals(2, ticket.getIndividualTickets().size());
        assertThrows(ParkException.class, () -> controller.deleteGroup("G01"));
    }

    @Test
    void groupTicketBlocksMemberDeletion() {
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(ParkException.class, () -> controller.deleteUser("ana@correo.com"));
        assertTrue(group.hasMember("ana@correo.com"));
    }

    @Test
    void lockedGroupBlocksMemberDeletionEvenWithoutOwnTicket() {
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.addUser(new User("22222222J", "maria@correo.com"));
        // maria is not part of the composition; joining is impossible while locked
        assertThrows(ParkException.class, () -> controller.addUserToGroup("G01", "maria@correo.com"));
        controller.deleteUser("maria@correo.com");
        assertTrue(controller.findUser("maria@correo.com").isEmpty());
    }
}
