package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketTest extends ParkTestSupport {

    private void groupOf(String groupId, int from, int count) {
        park.createGroup(groupId, "Group " + groupId);
        for (int i = from; i < from + count; i++) {
            user(i);
            park.addToGroup(groupId, email(i));
        }
    }

    @Test
    void individualTicketCreation() {
        User ana = user(1);
        IndividualTicket t = park.createTicket("T001", email(1), "2027-04-15");
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(ana, t.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t.getDate());
        assertNotNull(park.createTicket("T002", email(1), TODAY.toString()), "today is allowed");
    }

    @Test
    void individualTicketRejections() {
        user(1);
        park.createTicket("T001", email(1), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T002", email(1), "2027-03-31"));
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T003", "nadie@correo.com", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T001", email(1), "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T004", email(1), "2027-13-01"));
        assertNull(park.findTicket("T002"));
    }

    @Test
    void individualTicketDeletion() {
        user(1);
        park.createTicket("T001", email(1), "2027-04-15");
        park.createTicket("T002", email(1), "2027-04-15");
        park.deleteTicket("T001");
        assertNull(park.findTicket("T001"));

        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("T002"));
        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("T002"));
        assertNotNull(park.findTicket("T002"));
        assertFalse(park.findTicket("T002").isActive(park.getCurrentDate()));
    }

    @Test
    void groupTicketComposition() {
        groupOf("G01", 1, 2);
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(List.of("TG01:" + dni(1), "TG01:" + dni(2)),
                tg.getComposition().stream().map(Ticket::getId).toList());
        assertEquals(park.getGroup("G01"), tg.getGroup());
        assertEquals(new BigDecimal("80.00"), tg.getPrice());
        assertNotNull(park.findTicket("TG01:" + dni(1)));
    }

    @Test
    void sixMembersAt40() {
        groupOf("G02", 1, 6);
        GroupTicket tg = park.createGroupTicket("TG02", "G02", "2027-04-15");
        for (IndividualTicket t : tg.getComposition()) {
            assertEquals(new BigDecimal("32.00"), t.getPrice());
        }
        assertEquals(new BigDecimal("192.00"), tg.getPrice());
    }

    @Test
    void discountBrackets() {
        assertEquals(0, GroupTicket.discountFor(1));
        assertEquals(0, GroupTicket.discountFor(2));
        assertEquals(10, GroupTicket.discountFor(3));
        assertEquals(10, GroupTicket.discountFor(5));
        assertEquals(20, GroupTicket.discountFor(6));
        assertEquals(20, GroupTicket.discountFor(10));
        assertEquals(50, GroupTicket.discountFor(11));

        groupOf("G11", 1, 11);
        GroupTicket tg = park.createGroupTicket("TG11", "G11", "2027-04-15");
        assertEquals(new BigDecimal("20.00"), tg.getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("220.00"), tg.getPrice());
    }

    @Test
    void groupTicketUsesCurrentPrice() {
        park.setPrice(new BigDecimal("45.50"));
        groupOf("G01", 1, 3);
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(new BigDecimal("40.95"), tg.getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("122.85"), tg.getPrice());
    }

    @Test
    void groupTicketRejections() {
        park.createGroup("G00", "Empty");
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG00", "G00", "2027-04-15"));
        groupOf("G01", 1, 2);
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG01", "G99", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG01", "G01", "2027-03-01"));
        park.createTicket("T001", email(1), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("T001", "G01", "2027-04-15"));
    }

    @Test
    void derivedIdConflictCreatesNothing() {
        groupOf("G01", 1, 2);
        park.createTicket("TG01:" + dni(2), email(2), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:" + dni(1)));
    }

    @Test
    void sharedIdSpaceForIndividualTickets() {
        groupOf("G01", 1, 1);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("TG01", email(1), "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("TG01:" + dni(1), email(1), "2027-04-15"));
    }

    @Test
    void groupTicketDeletion() {
        groupOf("G01", 1, 2);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("TG01:" + dni(1)));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("TG01"));
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:" + dni(1)));

        park.createGroupTicket("TG02", "G01", "2027-04-15");
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        assertThrows(IllegalArgumentException.class, () -> park.deleteGroupTicket("TG02"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteGroupTicket("T404"));
        assertNotNull(park.findTicket("TG02"));
    }

    @Test
    void compositionFixedAfterGroupChanges() {
        groupOf("G01", 1, 2);
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        park.removeFromGroup("G01", email(1));
        assertEquals(2, tg.getComposition().size());
    }

    @Test
    void userDeletionRules() {
        user(1);
        park.createTicket("T001", email(1), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser(email(1), "Clave1!"));
        park.deleteTicket("T001");
        park.deleteUser(email(1), "Clave1!");
        assertNull(park.getUserService().findByEmail(email(1)));
    }

    @Test
    void todaysTicketBlocksUserDeletion() {
        user(1);
        park.createTicket("T001", email(1), TODAY.toString());
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser(email(1), "Clave1!"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("T001"));
    }

    @Test
    void groupCompositionTicketBlocksUserDeletion() {
        groupOf("G01", 1, 2);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser(email(1), "Clave1!"));
        assertNotNull(park.getUserService().findByEmail(email(1)));
    }

    @Test
    void lockedGroupBlocksUserDeletionEvenWithoutOwnTicket() {
        groupOf("G01", 1, 1);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        // Once TG01 is historical, user 2 joins G01 without being in TG01's composition.
        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        user(2);
        park.addToGroup("G01", email(2));
        // Moving the system date back makes TG01 active again, so G01 is locked.
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        assertTrue(park.isLocked(park.getGroup("G01")));
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser(email(2), "Clave1!"));
        assertNotNull(park.getUserService().findByEmail(email(2)));
    }

    @Test
    void userRemovedFromGroupsAndHistoricalTicketsKept() {
        groupOf("G01", 1, 2);
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        IndividualTicket t = park.createTicket("T001", email(1), "2027-04-15");
        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        User u1 = park.getUserService().findByEmail(email(1));
        park.deleteUser(email(1), "Clave1!");
        assertFalse(park.getGroup("G01").contains(u1));
        assertEquals(u1, t.getUser());
        assertTrue(tg.getHolders().contains(u1));
    }
}
