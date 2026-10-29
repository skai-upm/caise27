package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserDeletionTest extends ParkFixture {

    @Test
    void deleteWithoutTickets() throws Exception {
        park.deleteUser(PEDRO, "Otra2#");
        assertNull(user(PEDRO));
        restart();
        assertNull(user(PEDRO));
    }

    @Test
    void credentialsChecked() {
        assertThrows(OperationException.class, () -> park.deleteUser(PEDRO, "Otra2?"));
        assertThrows(OperationException.class, () -> park.deleteUser("nadie@correo.com", "Otra2#"));
        assertNotNull(user(PEDRO));
    }

    @Test
    void activeIndividualTicketBlocksUntilDeletedOrHistorical() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        park.createTicket("T002", ANA, "2027-04-20");
        assertThrows(OperationException.class, () -> park.deleteUser(ANA, "Clave1!"));
        park.deleteTicket("T002");
        assertThrows(OperationException.class, () -> park.deleteUser(ANA, "Clave1!"));
        park.setCurrentDate("2027-04-16");
        User ana = user(ANA);
        park.deleteUser(ANA, "Clave1!");
        assertNull(user(ANA));
        assertSame(ana, ((IndividualTicket) park.findTicket("T001")).getUser());
    }

    @Test
    void activeGroupTicketCompositionBlocks() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-15");
        assertThrows(OperationException.class, () -> park.deleteUser(ANA, "Clave1!"));
    }

    @Test
    void lockedGroupMembershipBlocksEvenWithoutOwnTicket() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-20");
        park.setCurrentDate("2027-04-21");
        park.addToGroup("G01", PEDRO);
        // Moving the date back makes TG01 active again: G01 is locked, Pedro is a member
        // but not part of TG01's composition and has no ticket of his own.
        park.setCurrentDate("2027-04-16");
        assertThrows(OperationException.class, () -> park.deleteUser(PEDRO, "Otra2#"));
        assertNotNull(user(PEDRO));
        park.setCurrentDate("2027-04-21");
        park.deleteUser(PEDRO, "Otra2#");
        assertEquals(List.of(user(ANA)), park.findGroup("G01").getMembers());
    }

    @Test
    void deletionRemovesUserFromModifiableGroups() throws Exception {
        park.createGroup("G01", "Familia");
        park.createGroup("G02", "Amigos");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        park.addToGroup("G02", PEDRO);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteUser(PEDRO, "Otra2#");
        assertEquals(List.of(user(ANA)), park.findGroup("G01").getMembers());
        assertTrue(park.findGroup("G02").getMembers().isEmpty());
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getUsers().size());
        restart();
        assertEquals(List.of(user(ANA)), park.findGroup("G01").getMembers());
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getUsers().size());
    }
}
