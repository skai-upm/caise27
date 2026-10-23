package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.persistence.DataPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDeletionTest {

    @TempDir
    Path dir;

    private Park park;
    private GroupController groups;
    private TicketController tickets;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        groups = GroupController.getInstance();
        tickets = TicketController.getInstance();
    }

    @Test
    void martaCanBeDeletedAfterFutureTicketIsDeleted() throws ParkException {
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
        assertThrows(ParkException.class, () -> UserController.getInstance().delete("marta@correo.com", "Marta4%"));

        tickets.deleteTicket("T002");
        UserController.getInstance().delete("marta@correo.com", "Marta4%");
        assertNull(park.findUserByEmail("marta@correo.com"));
    }

    @Test
    void todaysTicketBlocksDeletion() throws ParkException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        assertThrows(ParkException.class, () -> UserController.getInstance().delete("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUserByEmail("ana@correo.com"));
    }

    @Test
    void groupTicketComponentBlocksDeletion() throws ParkException {
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.plusDays(2));
        assertThrows(ParkException.class, () -> UserController.getInstance().delete("ana@correo.com", "Clave1!"));
    }

    @Test
    void lockedGroupMembershipBlocksDeletion() throws Exception {
        // A member without a ticket in the group's active group ticket cannot be produced
        // through commands, so the state is seeded through the data files.
        park.save();
        DataPaths paths = new DataPaths(dir);
        Files.write(paths.groups(), List.of("G01;Familia;87654321X;00000000T"));
        Files.write(paths.tickets(), List.of(
                "GROUP;TG01;2027-04-20;G01",
                "INDIVIDUAL;TG01:87654321X;2027-04-20;40.00;0;TG01;87654321X;pedro@correo.com;Pedro;Lopez Martin"));
        park = ParkFixture.restart(dir);

        assertFalse(park.hasActiveTicket(park.findUserByEmail("marta@correo.com")));
        ParkException e = assertThrows(ParkException.class,
                () -> UserController.getInstance().delete("marta@correo.com", "Marta4%"));
        assertTrue(e.getMessage().contains("locked"));
    }

    @Test
    void deletionRemovesUserFromGroupsAndKeepsHistoricalTickets() throws ParkException {
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY);
        IndividualTicket single = tickets.createTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));

        UserController.getInstance().delete("ana@correo.com", "Clave1!");

        assertNull(park.findUserByEmail("ana@correo.com"));
        assertFalse(park.findGroup("G01").getMembers().stream()
                .anyMatch(u -> u.getEmail().equals("ana@correo.com")));
        assertEquals(1, park.findGroup("G01").size());
        assertEquals("ana@correo.com", single.getUser().getEmail());
        assertNotNull(park.findTicket("TG01:12345678Z"));
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(ParkException.class, () -> UserController.getInstance().delete("ana@correo.com", "Wrong1!"));
    }
}
