package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserDeletionTest extends ParkTestSupport {

    @Test
    void activeIndividualTicketBlocksDeletion() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> users.delete(email(1), "Clave1!"));
        assertNotNull(park.getUsers().findByEmail(email(1)));
    }

    @Test
    void futureTicketCanBeDeletedFirst() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-20");
        assertThrows(IllegalArgumentException.class, () -> users.delete(email(1), "Clave1!"));
        tickets.deleteIndividual("T001");
        users.delete(email(1), "Clave1!");
        assertNull(park.getUsers().findByEmail(email(1)));
    }

    @Test
    void activeComposedTicketBlocksDeletion() {
        groupWith("G01", 2);
        tickets.createGroup("TG01", "G01", "2027-04-10");
        system.setDate("2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> users.delete(email(1), "Clave1!"));
    }

    @Test
    void lockedGroupBlocksDeletionOfMemberWithoutTicket() {
        groupWith("G01", 1);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        user(2);
        system.setDate("2027-04-16");
        groups.add("G01", email(2));
        system.setDate("2027-04-10");
        // G01 is locked again by TG01, which user 2 does not hold
        assertEquals(0, park.getTickets().ticketsOf(park.getUsers().findByEmail(email(2))).size());
        assertThrows(IllegalArgumentException.class, () -> users.delete(email(2), "Clave1!"));
        assertNotNull(park.getUsers().findByEmail(email(2)));
    }

    @Test
    void historicalTicketsKeptAndUserRemovedFromGroups() {
        groupWith("G01", 2);
        User ana = park.getUsers().findByEmail(email(1));
        tickets.createIndividual("T001", email(1), "2027-04-10");
        tickets.createGroup("TG01", "G01", "2027-04-10");
        system.setDate("2027-04-11");
        users.delete(email(1), "Clave1!");
        assertNull(park.getUsers().findByEmail(email(1)));
        assertFalse(park.getGroups().findById("G01").contains(ana));
        assertEquals(1, park.getGroups().findById("G01").getMembers().size());
        assertSame(ana, ((IndividualTicket) park.getTickets().findById("T001")).getUser());
        assertEquals(2, ((GroupTicket) park.getTickets().findById("TG01")).getComposition().size());
    }
}
