package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupTest extends ParkFixture {

    @BeforeEach
    void createGroup() throws Exception {
        park.createGroup("G01", "Familia Garcia");
    }

    @Test
    void createEmptyGroup() {
        Group g = park.findGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());
    }

    @Test
    void duplicateOrEmptyIdentifierRejected() {
        assertThrows(OperationException.class, () -> park.createGroup("G01", "Otro"));
        assertThrows(OperationException.class, () -> park.createGroup("", "Otro"));
        assertEquals(1, park.getGroups().size());
    }

    @Test
    void addAndRemoveMembers() throws Exception {
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        assertEquals(List.of(user(ANA), user(PEDRO)), park.findGroup("G01").getMembers());
        park.removeFromGroup("G01", PEDRO);
        assertEquals(List.of(user(ANA)), park.findGroup("G01").getMembers());
    }

    @Test
    void invalidMembershipChangesRejected() throws Exception {
        park.addToGroup("G01", ANA);
        assertThrows(OperationException.class, () -> park.addToGroup("G01", ANA));
        assertThrows(OperationException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(OperationException.class, () -> park.addToGroup("G99", ANA));
        assertThrows(OperationException.class, () -> park.removeFromGroup("G01", PEDRO));
        assertThrows(OperationException.class, () -> park.removeFromGroup("G99", ANA));
        assertEquals(List.of(user(ANA)), park.findGroup("G01").getMembers());
    }

    @Test
    void activeGroupTicketLocksGroupUntilHistorical() throws Exception {
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertTrue(park.isGroupLocked(park.findGroup("G01")));
        assertThrows(OperationException.class, () -> park.addToGroup("G01", PEDRO));
        assertThrows(OperationException.class, () -> park.removeFromGroup("G01", ANA));
        assertThrows(OperationException.class, () -> park.deleteGroup("G01"));

        park.setCurrentDate("2027-04-16");
        assertFalse(park.isGroupLocked(park.findGroup("G01")));
        park.addToGroup("G01", PEDRO);
        park.removeFromGroup("G01", ANA);
    }

    @Test
    void deletingFutureTicketUnlocksWhenNoOtherActiveTicket() throws Exception {
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-20");
        park.createGroupTicket("TG02", "G01", "2027-04-21");
        park.deleteGroupTicket("TG01");
        assertThrows(OperationException.class, () -> park.addToGroup("G01", PEDRO));
        park.deleteGroupTicket("TG02");
        park.addToGroup("G01", PEDRO);
    }

    @Test
    void deleteGroupKeepsHistoricalTicketsWithNullGroup() throws Exception {
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertNotNull(park.findTicket("TG01"));
        assertNull(ticket.getGroup());
        assertEquals(2, ticket.getComposition().size());
        assertEquals("80.00", ticket.getPrice().toPlainString());
    }

    @Test
    void deleteGroupWithoutTickets() throws Exception {
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertThrows(OperationException.class, () -> park.deleteGroup("G01"));
    }
}
