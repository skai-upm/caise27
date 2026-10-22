package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.IndividualTicket;
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
    private UserController users;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        users = park.getUserController();
    }

    @Test
    void activeIndividualTicketBlocksDeletion() {
        park.getTicketController().createIndividual("T001", "marta@correo.com", "2027-04-15");

        assertThrows(ParkException.class, () -> users.delete("marta@correo.com", "Marta4%"));
        assertTrue(park.getRepositories().users().findByEmail("marta@correo.com").isPresent());
    }

    @Test
    void deletionAllowedAfterRemovingFutureTicket() {
        park.getTicketController().createIndividual("T002", "marta@correo.com", "2027-04-20");
        assertThrows(ParkException.class, () -> users.delete("marta@correo.com", "Marta4%"));

        park.getTicketController().deleteIndividual("T002");
        users.delete("marta@correo.com", "Marta4%");

        assertTrue(park.getRepositories().users().findByEmail("marta@correo.com").isEmpty());
    }

    @Test
    void lockedGroupBlocksDeletion() {
        park.getGroupController().create("G01", "Familia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemController().setDate("2027-04-16");
        park.getGroupController().create("G02", "Otro");
        park.getGroupController().addMember("G02", "pedro@correo.com");
        park.getTicketController().createGroup("TG02", "G02", "2027-04-16");

        // Pedro holds an active component of TG02; Ana's TG01 is historical now
        assertThrows(ParkException.class, () -> users.delete("pedro@correo.com", "Otra2#"));
        users.delete("ana@correo.com", "Clave1!");

        assertFalse(park.getRepositories().groups().findById("G01").orElseThrow().getMembers().stream()
                .anyMatch(u -> u.getEmail().equals("ana@correo.com")));
    }

    @Test
    void memberOfLockedGroupWithoutOwnTicketCannotBeDeleted() {
        park.getGroupController().create("G01", "Familia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-20");
        // Laura joins once TG01 is historical, then the date moves back and TG01 is active again
        park.getSystemController().setDate("2027-04-21");
        park.getGroupController().addMember("G01", "laura@correo.com");
        park.getSystemController().setDate("2027-04-15");

        assertFalse(park.getRepositories().tickets().hasActiveTicket(park.getRepositories().users().findByEmail("laura@correo.com").orElseThrow(),
                park.getState().getCurrentDate()));
        assertThrows(ParkException.class, () -> users.delete("laura@correo.com", "Laura3$"));
        assertTrue(park.getRepositories().users().findByEmail("laura@correo.com").isPresent());
    }

    @Test
    void historicalTicketsSurviveUserDeletion() {
        IndividualTicket ticket = park.getTicketController().createIndividual("T001", "marta@correo.com", "2027-04-15");
        park.getSystemController().setDate("2027-04-16");

        users.delete("marta@correo.com", "Marta4%");

        assertEquals(ticket, park.getRepositories().tickets().findById("T001").orElseThrow());
        assertEquals("00000000T", ticket.getUser().getDni());
    }

    @Test
    void deletedUserLeavesUnlockedGroups() {
        park.getGroupController().create("G01", "Familia");
        park.getGroupController().addMember("G01", "laura@correo.com");

        users.delete("laura@correo.com", "Laura3$");

        assertTrue(park.getRepositories().groups().findById("G01").orElseThrow().isEmpty());
    }
}
