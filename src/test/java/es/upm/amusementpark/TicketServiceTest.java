package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketServiceTest extends ParkTestSupport {

    @Test
    void individualTicketUsesCurrentPrice() {
        User ana = user(1);
        IndividualTicket t1 = tickets.createIndividual("T001", email(1), "2027-04-15");
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertSame(ana, t1.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t1.getDate());
        system.setPrice("45.50");
        IndividualTicket t2 = tickets.createIndividual("T002", email(1), "2027-04-15");
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
    }

    @Test
    void todayAllowedPastRejected() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> tickets.createIndividual("T002", email(1), "2027-04-09"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createIndividual("T003", email(1), "2027-13-01"));
        assertNull(park.getTickets().findById("T002"));
    }

    @Test
    void unknownUserAndDuplicateIdRejected() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        assertThrows(IllegalArgumentException.class,
                () -> tickets.createIndividual("T002", "nadie@correo.com", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createIndividual("T001", email(1), "2027-04-15"));
    }

    @Test
    void individualIdCannotClashWithGroupOrComposedIds() {
        groupWith("G01", 1);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.createIndividual("TG01", email(1), "2027-04-15"));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.createIndividual("TG01:" + dni(1), email(1), "2027-04-15"));
    }

    @Test
    void deletionOnlyStrictlyFuture() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-11");
        tickets.createIndividual("T002", email(1), "2027-04-10");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteIndividual("T002"));
        tickets.deleteIndividual("T001");
        assertNull(park.getTickets().findById("T001"));
        system.setDate("2027-04-12");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteIndividual("T002"));
        assertNotNull(park.getTickets().findById("T002"));
    }

    @Test
    void ticketDeleteRefusesGroupAndComposedTickets() {
        groupWith("G01", 2);
        tickets.createGroup("TG01", "G01", "2027-04-20");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteIndividual("TG01"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteIndividual("TG01:" + dni(1)));
        assertNotNull(park.getTickets().findById("TG01:" + dni(1)));
    }

    @Test
    void groupTicketDeleteRefusesIndividualTickets() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-20");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroup("T001"));
    }

    @Test
    void groupTicketComposition() {
        Group group = groupWith("G01", 2);
        GroupTicket ticket = tickets.createGroup("TG01", "G01", "2027-04-15");
        assertSame(group, ticket.getGroup());
        List<IndividualTicket> composition = ticket.getComposition();
        assertEquals(2, composition.size());
        assertEquals("TG01:" + dni(1), composition.get(0).getId());
        assertEquals("TG01:" + dni(2), composition.get(1).getId());
        assertEquals(email(1), composition.get(0).getUser().getEmail());
        assertTrue(park.getTickets().isComposed(composition.get(0)));
    }

    @Test
    void compositionIsFixed() {
        groupWith("G01", 2);
        user(3);
        GroupTicket ticket = tickets.createGroup("TG01", "G01", "2027-04-15");
        system.setDate("2027-04-16");
        groups.add("G01", email(3));
        groups.remove("G01", email(1));
        assertEquals(List.of("TG01:" + dni(1), "TG01:" + dni(2)),
                ticket.getComposition().stream().map(Ticket::getId).toList());
    }

    @Test
    void emptyGroupRejected() {
        groups.create("G01", "Empty");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroup("TG01", "G01", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroup("TG01", "G99", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> {
            groupWith("G02", 1);
            tickets.createGroup("TG02", "G02", "2027-04-01");
        });
    }

    @Test
    void derivedIdClashIsAtomic() {
        groupWith("G01", 3);
        tickets.createIndividual("TG01:" + dni(2), email(2), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroup("TG01", "G01", "2027-04-15"));
        assertNull(park.getTickets().findById("TG01"));
        assertNull(park.getTickets().findById("TG01:" + dni(1)));
        assertEquals(1, park.getTickets().findAll().size());
        assertFalse(park.isLocked(park.getGroups().findById("G01")));
    }

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
            "11,50,220.00"})
    void discountBrackets(int members, int discount, String total) {
        groupWith("G01", members);
        GroupTicket ticket = tickets.createGroup("TG01", "G01", "2027-04-15");
        assertEquals(discount, GroupTicket.discountFor(members));
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }

    @Test
    void sixMembersCostThirtyTwoEach() {
        groupWith("G02", 6);
        GroupTicket ticket = tickets.createGroup("TG02", "G02", "2027-04-15");
        for (IndividualTicket member : ticket.getComposition()) {
            assertEquals(new BigDecimal("32.00"), member.getPrice());
        }
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
    }

    @Test
    void groupTicketDeletionRemovesComposition() {
        groupWith("G01", 2);
        tickets.createGroup("TG01", "G01", "2027-04-20");
        tickets.deleteGroup("TG01");
        assertNull(park.getTickets().findById("TG01"));
        assertNull(park.getTickets().findById("TG01:" + dni(1)));
        assertTrue(park.getTickets().findAll().isEmpty());
    }
}
