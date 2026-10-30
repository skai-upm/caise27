package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketControllerTest extends ParkFixture {

    private TicketController tickets;
    private GroupController groups;

    @BeforeEach
    void setUp() {
        tickets = new TicketController(park);
        groups = new GroupController(park);
        groups.create("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    @Test
    void createIndividualTicketAtCurrentPrice() {
        IndividualTicket t = tickets.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertSame(ana, t.getUser());
        park.setPrice("45.50");
        IndividualTicket t2 = tickets.createTicket("T002", "ana@correo.com", "2027-04-16");
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
    }

    @Test
    void individualTicketRejections() {
        assertThrows(IllegalArgumentException.class, () -> tickets.createTicket("T002", "ana@correo.com", "2027-04-14"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createTicket("T003", "nobody@correo.com", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createTicket("T004", "ana@correo.com", "15-04-2027"));
        tickets.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.createTicket("T001", "pedro@correo.com", "2027-04-15"));
        assertEquals(1, park.tickets().findAll().size());
    }

    @Test
    void deleteIndividualTicketOnlyInFuture() {
        tickets.createTicket("T001", "ana@correo.com", "2027-04-16");
        tickets.createTicket("T002", "ana@correo.com", "2027-04-15");
        tickets.deleteTicket("T001");
        assertTrue(park.tickets().findById("T001").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteTicket("T002"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteTicket("T999"));
        assertTrue(park.tickets().findById("T002").isPresent());
    }

    @Test
    void createGroupTicketWithComposition() {
        GroupTicket g = tickets.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(new BigDecimal("80.00"), g.getPrice());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                g.getIndividualTickets().stream().map(Ticket::getId).toList());
        assertSame(park.groups().findById("G01").orElseThrow(), g.getGroup());
        assertTrue(park.tickets().existsId("TG01:87654321X"));
    }

    @Test
    void sixMembersCost192() {
        for (int i = 1; i <= 4; i++) {
            groups.addUser("G01", createUser(i).getEmail());
        }
        assertEquals(new BigDecimal("192.00"), tickets.createGroupTicket("TG02", "G01", "2027-04-15").getPrice());
    }

    @Test
    void groupTicketRejections() {
        groups.create("G02", "Vacio");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G02", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G99", "2027-04-15"));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G01", "2027-04-14"));
        tickets.createTicket("T001", "ana@correo.com", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("T001", "G01", "2027-04-15"));
        assertEquals(1, park.tickets().findAll().size());
    }

    @Test
    void derivedIdConflictCreatesNothing() {
        tickets.createTicket("TG01:87654321X", "ana@correo.com", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G01", "2027-04-15"));
        assertTrue(park.tickets().findById("TG01").isEmpty());
        assertTrue(park.tickets().findById("TG01:12345678Z").isEmpty());
    }

    @Test
    void compositionTicketsCannotBeDeletedDirectly() {
        tickets.createGroupTicket("TG01", "G01", "2027-04-20");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteTicket("TG01:12345678Z"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteTicket("TG01"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01:12345678Z"));
        assertTrue(park.tickets().existsId("TG01:12345678Z"));
    }

    @Test
    void deleteGroupTicketWithComposition() {
        tickets.createGroupTicket("TG01", "G01", "2027-04-20");
        tickets.deleteGroupTicket("TG01");
        assertFalse(park.tickets().existsId("TG01"));
        assertFalse(park.tickets().existsId("TG01:12345678Z"));
    }

    @Test
    void groupTicketForTodayIsKept() {
        tickets.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertTrue(park.tickets().existsId("TG01"));
    }

    @Test
    void compositionUnchangedWhenGroupChanges() {
        tickets.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setDate("2027-04-16");
        groups.removeUser("G01", "pedro@correo.com");
        assertEquals(2, park.tickets().findById("TG01").orElseThrow().getHolders().size());
    }

    @Test
    void ticketsSurviveRestartLinkedToLiveObjects() {
        tickets.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.setPrice("50");
        tickets.createGroupTicket("TG01", "G01", "2027-04-16");
        restart();

        IndividualTicket t = (IndividualTicket) park.tickets().findById("T001").orElseThrow();
        assertSame(park.users().findByEmail("ana@correo.com").orElseThrow(), t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        GroupTicket g = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertSame(park.groups().findById("G01").orElseThrow(), g.getGroup());
        assertEquals(new BigDecimal("100.00"), g.getPrice());
        assertEquals(2, g.getIndividualTickets().size());
    }

    @Test
    void deletedUsersHistoricalTicketSurvivesRestart() {
        tickets.createTicket("T001", "pedro@correo.com", "2027-04-15");
        park.setDate("2027-04-16");
        groups.removeUser("G01", "pedro@correo.com");
        users.delete("pedro@correo.com", "Otra2#");
        users.create("Nuevo", "Usuario", "87654321X", "nuevo@correo.com", "Clave1!", "600", "1111222233334444");
        restart();

        IndividualTicket t = (IndividualTicket) park.tickets().findById("T001").orElseThrow();
        assertEquals("pedro@correo.com", t.getUser().getEmail());
        assertEquals("87654321X", t.getUser().getDni());
        assertNotSame(park.users().findByDni("87654321X").orElseThrow(), t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
    }
}
