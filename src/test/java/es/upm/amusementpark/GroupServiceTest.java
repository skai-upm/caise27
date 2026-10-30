package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GroupServiceTest extends ParkTestSupport {

    @Test
    void groupCreatedEmptyAndFilledProgressively() {
        groups.create("G01", "Familia Garcia");
        assertTrue(park.getGroups().findById("G01").getMembers().isEmpty());
        user(1);
        user(2);
        groups.add("G01", email(1));
        groups.add("G01", email(2));
        assertEquals(2, park.getGroups().findById("G01").getMembers().size());
    }

    @Test
    void duplicateIdentifierRejected() {
        groups.create("G01", "A");
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "B"));
    }

    @Test
    void duplicateMemberRejected() {
        groupWith("G01", 1);
        assertThrows(IllegalArgumentException.class, () -> groups.add("G01", email(1)));
        assertEquals(1, park.getGroups().findById("G01").getMembers().size());
    }

    @Test
    void unknownGroupOrUserRejected() {
        user(1);
        groups.create("G01", "A");
        assertThrows(IllegalArgumentException.class, () -> groups.add("G99", email(1)));
        assertThrows(IllegalArgumentException.class, () -> groups.add("G01", "nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.remove("G01", email(1)));
    }

    @Test
    void memberRemovedAndPersisted() {
        groupWith("G01", 2);
        groups.remove("G01", email(1));
        restart();
        Group group = park.getGroups().findById("G01");
        assertEquals(1, group.getMembers().size());
        assertEquals(email(2), group.getMembers().get(0).getEmail());
    }

    @Test
    void activeGroupTicketLocksGroup() {
        groupWith("G01", 2);
        user(3);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        assertTrue(park.isLocked(park.getGroups().findById("G01")));
        assertThrows(IllegalArgumentException.class, () -> groups.add("G01", email(3)));
        assertThrows(IllegalArgumentException.class, () -> groups.remove("G01", email(1)));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertEquals(2, park.getGroups().findById("G01").getMembers().size());
    }

    @Test
    void historicalTicketUnlocks() {
        groupWith("G01", 2);
        user(3);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        system.setDate("2027-04-16");
        groups.add("G01", email(3));
        assertEquals(3, park.getGroups().findById("G01").getMembers().size());
    }

    @Test
    void deletingFutureTicketUnlocksOnlyWhenNoOtherActiveTicket() {
        groupWith("G01", 2);
        user(3);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        tickets.createGroup("TG02", "G01", "2027-04-20");
        tickets.deleteGroup("TG01");
        assertThrows(IllegalArgumentException.class, () -> groups.add("G01", email(3)));
        tickets.deleteGroup("TG02");
        groups.add("G01", email(3));
    }

    @Test
    void currentDayTicketBlocksDeletion() {
        groupWith("G01", 1);
        tickets.createGroup("TG01", "G01", "2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroup("TG01"));
    }

    @Test
    void deletionKeepsHistoricalTicketsWithNullGroup() {
        groupWith("G01", 3);
        GroupTicket ticket = tickets.createGroup("TG01", "G01", "2027-04-10");
        system.setDate("2027-04-11");
        groups.delete("G01");
        assertNull(park.getGroups().findById("G01"));
        GroupTicket kept = (GroupTicket) park.getTickets().findById("TG01");
        assertNull(kept.getGroup());
        assertEquals(3, kept.getComposition().size());
        assertEquals(new BigDecimal("108.00"), kept.getPrice());
        assertEquals(ticket.getDate(), kept.getDate());
    }
}
