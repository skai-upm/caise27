package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupTest extends ParkTestSupport {

    @Test
    void createAddRemove() {
        User a = user(1);
        User b = user(2);
        park.createGroup("G01", "Familia Garcia");
        assertTrue(park.getGroup("G01").getMembers().isEmpty());
        park.addToGroup("G01", email(1));
        park.addToGroup("G01", email(2));
        assertEquals(List.of(a, b), park.getGroup("G01").getMembers());
        park.removeFromGroup("G01", email(1));
        assertEquals(List.of(b), park.getGroup("G01").getMembers());
    }

    @Test
    void rejectsInvalidOperations() {
        user(1);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(1));
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Other"));
        assertThrows(ParkException.class, () -> park.createGroup("", "Other"));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", email(1)));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G99", email(1)));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "nadie@correo.com"));
        assertEquals(1, park.getGroup("G01").getMembers().size());
    }

    @Test
    void lockedWhileActiveGroupTicket() {
        user(1);
        user(2);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(1));
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        Group g = park.getGroup("G01");
        assertTrue(park.isLocked(g));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", email(2)));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", email(1)));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));

        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        assertTrue(park.isLocked(g), "ticket for the current day still locks");
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));

        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        assertFalse(park.isLocked(g));
        park.addToGroup("G01", email(2));
        assertEquals(2, g.getMembers().size());
    }

    @Test
    void unlockedAfterFutureTicketDeleted() {
        user(1);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(1));
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createGroupTicket("TG02", "G01", "2027-04-20");
        park.deleteGroupTicket("TG01");
        assertTrue(park.isLocked(park.getGroup("G01")), "TG02 still active");
        park.deleteGroupTicket("TG02");
        assertFalse(park.isLocked(park.getGroup("G01")));
    }

    @Test
    void deleteKeepsHistoricalTickets() {
        user(1);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(1));
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate(LocalDate.of(2027, 5, 1));
        park.deleteGroup("G01");
        assertNull(park.getGroup("G01"));
        assertEquals(tg, park.findTicket("TG01"));
        assertNull(tg.getGroup());
        assertEquals(1, tg.getComposition().size());
    }
}
