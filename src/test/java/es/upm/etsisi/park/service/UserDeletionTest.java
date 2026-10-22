package es.upm.etsisi.park.service;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.model.IndividualTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDeletionTest {

    @TempDir
    Path dir;

    private Park park;
    private UserService users;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        users = park.getUserService();
    }

    @Test
    void activeIndividualTicketBlocksDeletion() {
        park.getTicketService().createIndividual("T001", "marta@correo.com", "2027-04-15");

        assertThrows(UserOperationException.class, () -> users.delete("marta@correo.com", "Marta4%"));
        assertTrue(park.getUsers().findByEmail("marta@correo.com").isPresent());
    }

    @Test
    void deletionAllowedAfterRemovingFutureTicket() {
        park.getTicketService().createIndividual("T002", "marta@correo.com", "2027-04-20");
        assertThrows(UserOperationException.class, () -> users.delete("marta@correo.com", "Marta4%"));

        park.getTicketService().deleteIndividual("T002");
        users.delete("marta@correo.com", "Marta4%");

        assertTrue(park.getUsers().findByEmail("marta@correo.com").isEmpty());
    }

    @Test
    void lockedGroupBlocksDeletion() {
        park.getGroupService().create("G01", "Familia");
        park.getGroupService().addMember("G01", "ana@correo.com");
        park.getGroupService().addMember("G01", "pedro@correo.com");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemService().setDate("2027-04-16");
        park.getGroupService().create("G02", "Otro");
        park.getGroupService().addMember("G02", "pedro@correo.com");
        park.getTicketService().createGroup("TG02", "G02", "2027-04-16");

        // Pedro holds an active component of TG02; Ana's TG01 is historical now
        assertThrows(UserOperationException.class, () -> users.delete("pedro@correo.com", "Otra2#"));
        users.delete("ana@correo.com", "Clave1!");

        assertFalse(park.getGroups().findById("G01").orElseThrow().getMembers().stream()
                .anyMatch(u -> u.getEmail().equals("ana@correo.com")));
    }

    @Test
    void memberOfLockedGroupWithoutOwnTicketCannotBeDeleted() {
        park.getGroupService().create("G01", "Familia");
        park.getGroupService().addMember("G01", "ana@correo.com");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-20");
        // Laura joins once TG01 is historical, then the date moves back and TG01 is active again
        park.getSystemService().setDate("2027-04-21");
        park.getGroupService().addMember("G01", "laura@correo.com");
        park.getSystemService().setDate("2027-04-15");

        assertFalse(park.getTickets().hasActiveTicket(park.getUsers().findByEmail("laura@correo.com").orElseThrow(),
                park.getState().getCurrentDate()));
        assertThrows(UserOperationException.class, () -> users.delete("laura@correo.com", "Laura3$"));
        assertTrue(park.getUsers().findByEmail("laura@correo.com").isPresent());
    }

    @Test
    void historicalTicketsSurviveUserDeletion() {
        IndividualTicket ticket = park.getTicketService().createIndividual("T001", "marta@correo.com", "2027-04-15");
        park.getSystemService().setDate("2027-04-16");

        users.delete("marta@correo.com", "Marta4%");

        assertEquals(ticket, park.getTickets().findById("T001").orElseThrow());
        assertEquals("00000000T", ticket.getUser().getDni());
    }

    @Test
    void deletedUserLeavesUnlockedGroups() {
        park.getGroupService().create("G01", "Familia");
        park.getGroupService().addMember("G01", "laura@correo.com");

        users.delete("laura@correo.com", "Laura3$");

        assertTrue(park.getGroups().findById("G01").orElseThrow().isEmpty());
    }
}
