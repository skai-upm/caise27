package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketServiceTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO);
    }

    @Test
    void createsTicketForTodayWithBasePrice() {
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        IndividualTicket t = (IndividualTicket) f.park.findTicket("T001");
        assertEquals("ana@correo.com", t.getUser().getEmail());
        assertTrue(t.isValidOn(TestParks.TODAY));
    }

    @Test
    void rejectsPastDateUnknownUserDuplicateIdAndBadDate() {
        assertTrue(f.run("TICKET CREATE;T002;ana@correo.com;2027-04-14").startsWith("ERROR: date"));
        assertTrue(f.run("TICKET CREATE;T003;nadie@correo.com;2027-04-15").startsWith("ERROR: no user"));
        assertTrue(f.run("TICKET CREATE;T004;ana@correo.com;15-04-2027").startsWith("ERROR: invalid date"));
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(f.run("TICKET CREATE;T001;pedro@correo.com;2027-04-16").contains("already in use"));
        assertEquals(1, f.park.getTickets().size());
    }

    @Test
    void priceChangeAffectsOnlyNewTickets() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15", "SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", f.run("TICKET CREATE;T002;ana@correo.com;2027-04-16"));
        assertEquals("40.00", f.park.findTicket("T001").getPrice().toPlainString());
    }

    @Test
    void deletesOnlyFutureStandaloneTickets() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15", "TICKET CREATE;T002;ana@correo.com;2027-04-16");
        assertTrue(f.run("TICKET DELETE;T001").startsWith("ERROR"));
        assertEquals("TICKET DELETED", f.run("TICKET DELETE;T002"));
        assertNull(f.park.findTicket("T002"));
        assertTrue(f.run("TICKET DELETE;T999").startsWith("ERROR: no ticket"));
        f.run("SYSTEM DATE;2027-04-20");
        assertTrue(f.run("TICKET DELETE;T001").startsWith("ERROR"));
        assertNotNull(f.park.findTicket("T001"));
    }

    @Test
    void groupTicketCreatedWithDerivedIds() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com");
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: 80.00 EUR", f.run("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        GroupTicket g = (GroupTicket) f.park.findTicket("TG01");
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                g.getComposition().stream().map(Ticket::getId).toList());
        assertEquals(f.park.findGroup("G01"), g.getGroup());
        assertNotNull(f.park.findTicket("TG01:87654321X"));
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void groupDiscountBrackets(int members, String total) {
        f.createUsers(members);
        f.run("GROUP CREATE;G;Big");
        for (int i = 0; i < members; i++) {
            f.run("GROUP ADD;G;u" + i + "@correo.com");
        }
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: " + total + " EUR", f.run("GROUPTICKET CREATE;TG;G;2027-04-15"));
    }

    @Test
    void groupDiscountUsesCurrentPrice() {
        f.createUsers(3);
        f.run("GROUP CREATE;G;Trio", "GROUP ADD;G;u0@correo.com", "GROUP ADD;G;u1@correo.com", "GROUP ADD;G;u2@correo.com",
                "SYSTEM PRICE;50");
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: 135.00 EUR", f.run("GROUPTICKET CREATE;TG;G;2027-04-15"));
    }

    @Test
    void groupTicketRejections() {
        f.run("GROUP CREATE;G01;Familia", "GROUP CREATE;G02;Vacio", "GROUP ADD;G01;ana@correo.com");
        assertTrue(f.run("GROUPTICKET CREATE;TG02;G02;2027-04-15").contains("has no users"));
        assertTrue(f.run("GROUPTICKET CREATE;TG03;G99;2027-04-15").startsWith("ERROR: no group"));
        assertTrue(f.run("GROUPTICKET CREATE;TG04;G01;2027-04-14").startsWith("ERROR: date"));
        f.run("TICKET CREATE;T001;pedro@correo.com;2027-04-15");
        assertTrue(f.run("GROUPTICKET CREATE;T001;G01;2027-04-15").contains("already in use"));
        assertEquals(1, f.park.getTickets().size());
    }

    @Test
    void derivedIdCollisionCreatesNothing() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;pedro@correo.com", "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;TG03:12345678Z;pedro@correo.com;2027-04-15");
        assertTrue(f.run("GROUPTICKET CREATE;TG03;G01;2027-04-15").contains("TG03:12345678Z is already in use"));
        assertNull(f.park.findTicket("TG03"));
        assertNull(f.park.findTicket("TG03:87654321X"));
        assertTrue(f.run("TICKET CREATE;TG01:87654321X;ana@correo.com;2027-04-15").startsWith("TICKET CREATED"));
    }

    @Test
    void groupTicketDeletion() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-16", "TICKET CREATE;T001;ana@correo.com;2027-04-16");
        assertTrue(f.run("TICKET DELETE;TG01:12345678Z").contains("belongs to a group ticket"));
        assertTrue(f.run("TICKET DELETE;TG01").contains("use GROUPTICKET DELETE"));
        assertTrue(f.run("GROUPTICKET DELETE;T001").contains("is not a group ticket"));
        assertEquals("GROUPTICKET DELETED", f.run("GROUPTICKET DELETE;TG01"));
        assertNull(f.park.findTicket("TG01"));
        assertNull(f.park.findTicket("TG01:12345678Z"));
    }

    @Test
    void compositionFixedAfterMembershipChange() {
        f.run("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "SYSTEM DATE;2027-04-16", "GROUP REMOVE;G01;ana@correo.com");
        assertEquals(2, ((GroupTicket) f.park.findTicket("TG01")).getComposition().size());
    }

    @Test
    void usersAndGroupsHoldNoTicketReferences() {
        for (Class<?> type : List.of(User.class, Group.class)) {
            for (Field field : type.getDeclaredFields()) {
                assertFalse(field.getType().getSimpleName().contains("Ticket"), type + "." + field.getName());
                assertFalse(field.getGenericType().getTypeName().contains("Ticket"), type + "." + field.getName());
                assertFalse(field.getGenericType().getTypeName().contains("Attraction"), type + "." + field.getName());
            }
        }
    }

    @Test
    void ticketsShareCommonAbstraction() {
        assertTrue(Ticket.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(Ticket.class.isAssignableFrom(GroupTicket.class));
        assertEquals(0, GroupTicket.discountFor(2));
        assertEquals(10, GroupTicket.discountFor(3));
        assertEquals(20, GroupTicket.discountFor(6));
        assertEquals(50, GroupTicket.discountFor(11));
    }
}
