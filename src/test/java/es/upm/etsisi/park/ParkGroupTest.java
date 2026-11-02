package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkGroupTest extends ParkTestSupport {

    private User ana;

    @BeforeEach
    void setUp() {
        ana = ana();
        pedro();
        park.createGroup("G01", "Familia Garcia");
    }

    @Test
    void createsEmptyGroup() {
        Group g = park.findGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertTrue(g.getMembers().isEmpty());
    }

    @Test
    void duplicateIdRejected() {
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void addsUserOnlyOnce() {
        park.addToGroup("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "ana@correo.com"));
        assertEquals(List.of(ana), park.findGroup("G01").getMembers());
    }

    @Test
    void unknownUserOrGroupRejected() {
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G99"));
        assertTrue(park.findGroup("G01").getMembers().isEmpty());
    }

    @Test
    void removesMember() {
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.removeFromGroup("G01", "ana@correo.com");
        assertFalse(park.findGroup("G01").contains(ana));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
    }

    @Test
    void lockedByTodaysTicket() {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_S);
        assertTrue(park.isGroupLocked(park.findGroup("G01")));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        assertEquals(List.of(ana), park.findGroup("G01").getMembers());
    }

    @Test
    void historicalTicketsDoNotLock() {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_S);
        park.setToday(TOMORROW_S);
        assertFalse(park.isGroupLocked(park.findGroup("G01")));
        park.addToGroup("G01", "pedro@correo.com");
        assertEquals(2, park.findGroup("G01").getMembers().size());
    }

    @Test
    void unlockedAfterDeletingFutureTicket() {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TOMORROW_S);
        park.createGroupTicket("TG02", "G01", "2027-04-20");
        park.deleteGroupTicket("TG01");
        assertTrue(park.isGroupLocked(park.findGroup("G01")), "TG02 is still active");
        park.deleteGroupTicket("TG02");
        assertFalse(park.isGroupLocked(park.findGroup("G01")));
        park.addToGroup("G01", "pedro@correo.com");
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithoutReference() {
        park.addToGroup("G01", "ana@correo.com");
        GroupTicket tg = park.createGroupTicket("TG01", "G01", TODAY_S);
        park.setToday(TOMORROW_S);
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertNull(tg.getGroup());
        assertEquals(tg, park.findTicket("TG01"));
        assertEquals(List.of(ana), tg.getHolders());
    }

    @Test
    void lockIsNotStored() {
        for (Field f : Group.class.getDeclaredFields()) {
            assertFalse(f.getType() == boolean.class || f.getType() == Boolean.class, "no stored lock flag");
            assertFalse(Ticket.class.isAssignableFrom(f.getType()), "groups hold no tickets");
        }
    }
}
