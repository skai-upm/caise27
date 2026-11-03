package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserDeletionRulesTest {

    private static final String DELETE_ANA = "USER DELETE;ana@correo.com;Clave1!";

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO);
    }

    @Test
    void ticketForTodayBlocksDeletion() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(f.run(DELETE_ANA).contains("active tickets"));
        assertNotNull(f.park.findUser("ana@correo.com"));
    }

    @Test
    void futureTicketBlocksUntilDeleted() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-20");
        assertTrue(f.run(DELETE_ANA).contains("active tickets"));
        f.run("TICKET DELETE;T001");
        assertEquals("USER DELETED", f.run(DELETE_ANA));
    }

    @Test
    void groupTicketCompositionBlocksDeletion() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-20");
        assertTrue(f.run(DELETE_ANA).contains("active tickets"));
        assertNotNull(f.park.findUser("ana@correo.com"));
    }

    @Test
    void memberOfLockedGroupCannotBeDeletedWithoutOwnActiveTicket() {
        // Pedro's group ticket is for 2027-04-15; Ana joins once it is historical, then the date goes back,
        // so the group is locked again although Ana has no ticket of her own.
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;pedro@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SYSTEM DATE;2027-04-16", "GROUP ADD;G01;ana@correo.com", "SYSTEM DATE;2027-04-15");
        assertFalse(f.park.hasActiveTicket(f.park.findUser("ana@correo.com")));
        assertTrue(f.run(DELETE_ANA).contains("locked by an active group ticket"));
        assertTrue(f.park.findGroup("G01").contains(f.park.findUser("ana@correo.com")));
    }

    @Test
    void memberOfUnlockedGroupIsRemovedFromItOnDeletion() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com");
        assertEquals("USER DELETED", f.run(DELETE_ANA));
        assertEquals(1, f.park.findGroup("G01").getMembers().size());
    }

    @Test
    void historicalTicketsAllowDeletionAndAreRetained() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", f.run(DELETE_ANA));
        assertNull(f.park.findUser("ana@correo.com"));
        assertTrue(f.park.findGroup("G01").getMembers().isEmpty());
        IndividualTicket t = (IndividualTicket) f.park.findTicket("T001");
        assertEquals("12345678Z", t.getUser().getDni());
        assertEquals(1, ((GroupTicket) f.park.findTicket("TG01")).getComposition().size());
    }

    @Test
    void deletedUserLeavesAttractions() {
        f.run("ATTRACTION CREATE;A01;Dragon Rojo;20", "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION ENTER;A01;T001", "SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", f.run(DELETE_ANA));
        assertEquals("NO USERS INSIDE", f.run("ATTRACTION LIST;A01"));
    }
}
