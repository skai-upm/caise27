package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkTicketTest extends ParkTestSupport {

    private User ana;

    @BeforeEach
    void setUp() {
        ana = ana();
    }

    @Test
    void createsTicketForTodayAtCurrentPrice() {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", TODAY_S);
        assertSame(ana, t.getUser());
        assertEquals(TODAY, t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(List.of(ana), t.getHolders());
    }

    @Test
    void pastDateRejected() {
        assertThrows(ParkException.class, () -> tickets.create("T001", "ana@correo.com", YESTERDAY_S));
        assertNull(park.findTicket("T001"));
    }

    @Test
    void unknownUserOrBadDateRejected() {
        assertThrows(ParkException.class, () -> tickets.create("T001", "nadie@correo.com", TODAY_S));
        assertThrows(ParkException.class, () -> tickets.create("T001", "ana@correo.com", "15-04-2027"));
        assertThrows(ParkException.class, () -> tickets.create(" ", "ana@correo.com", TODAY_S));
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void sharedIdentifierSpace() {
        tickets.create("T001", "ana@correo.com", TODAY_S);
        assertThrows(ParkException.class, () -> tickets.create("T001", "ana@correo.com", TOMORROW_S));
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> tickets.createGroupTicket("T001", "G01", TOMORROW_S));
        tickets.createGroupTicket("TG01", "G01", TOMORROW_S);
        assertThrows(ParkException.class, () -> tickets.create("TG01", "ana@correo.com", TOMORROW_S));
        assertThrows(ParkException.class, () -> tickets.create("TG01:12345678Z", "ana@correo.com", TOMORROW_S));
    }

    @Test
    void deletionRules() {
        tickets.create("TODAY", "ana@correo.com", TODAY_S);
        tickets.create("FUTURE", "ana@correo.com", TOMORROW_S);
        assertThrows(ParkException.class, () -> tickets.delete("TODAY"));
        assertNotNull(park.findTicket("TODAY"));
        tickets.delete("FUTURE");
        assertNull(park.findTicket("FUTURE"));
        assertThrows(ParkException.class, () -> tickets.delete("NOPE"));
        park.setToday(TOMORROW_S);
        assertThrows(ParkException.class, () -> tickets.delete("TODAY"), "historical tickets are kept");
    }

    @Test
    void groupComponentsAndGroupTicketsCannotBeDeletedAsIndividual() {
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TOMORROW_S);
        ParkException e = assertThrows(ParkException.class, () -> tickets.delete("TG01:12345678Z"));
        assertTrue(e.getMessage().contains("group ticket"));
        assertThrows(ParkException.class, () -> tickets.delete("TG01"));
        assertNotNull(park.findTicket("TG01:12345678Z"));
    }

    @Test
    void activeIndividualTicketBlocksUserDeletion() {
        tickets.create("T001", "ana@correo.com", TODAY_S);
        assertThrows(UserException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void futureTicketMustBeDeletedFirst() {
        tickets.create("T001", "ana@correo.com", TOMORROW_S);
        assertThrows(UserException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        tickets.delete("T001");
        users.delete("ana@correo.com", "Clave1!");
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void historicalTicketDoesNotBlockAndKeepsUser() {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", TODAY_S);
        park.setToday(TOMORROW_S);
        users.delete("ana@correo.com", "Clave1!");
        assertNull(park.findUser("ana@correo.com"));
        assertSame(t, park.findTicket("T001"));
        assertEquals("12345678Z", t.getUser().getDni());
    }

    @Test
    void ticketHierarchyIsShared() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
        for (Field f : User.class.getDeclaredFields()) {
            assertFalse(java.util.Collection.class.isAssignableFrom(f.getType()), "users hold no ticket collections");
            assertFalse(Ticket.class.isAssignableFrom(f.getType()));
            assertFalse(Attraction.class.isAssignableFrom(f.getType()));
        }
    }
}
