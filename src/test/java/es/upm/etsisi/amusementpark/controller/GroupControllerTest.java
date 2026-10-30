package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupControllerTest extends ParkFixture {

    private GroupController groups;
    private TicketController tickets;

    @BeforeEach
    void setUp() {
        groups = new GroupController(park);
        tickets = new TicketController(park);
        groups.create("G01", "Familia Garcia");
    }

    private Group g01() {
        return park.groups().findById("G01").orElseThrow();
    }

    @Test
    void createEmptyGroup() {
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(g01().getMembers().isEmpty());
    }

    @Test
    void duplicateIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> groups.create("G01", "Otro"));
        assertEquals("Familia Garcia", g01().getName());
    }

    @Test
    void addAndRemoveMembers() {
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
        assertEquals(List.of(ana, pedro), g01().getMembers());
        groups.removeUser("G01", "pedro@correo.com");
        assertEquals(List.of(ana), g01().getMembers());
    }

    @Test
    void duplicateMemberRejected() {
        groups.addUser("G01", "ana@correo.com");
        assertThrows(IllegalArgumentException.class, () -> groups.addUser("G01", "ana@correo.com"));
        assertEquals(1, g01().getMembers().size());
    }

    @Test
    void unknownUserOrGroupRejected() {
        assertThrows(IllegalArgumentException.class, () -> groups.addUser("G01", "nobody@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.addUser("G99", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeUser("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G99"));
    }

    @Test
    void lockedByActiveGroupTicket() {
        groups.addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.plusDays(2).toString());
        assertTrue(groups.isLocked(g01()));
        assertThrows(IllegalArgumentException.class, () -> groups.addUser("G01", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeUser("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertEquals(List.of(ana), g01().getMembers());
    }

    @Test
    void unlockedWhenTicketBecomesHistorical() {
        groups.addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        park.setDate(TODAY.plusDays(1).toString());
        assertFalse(groups.isLocked(g01()));
        groups.addUser("G01", "pedro@correo.com");
        assertEquals(2, g01().getMembers().size());
    }

    @Test
    void unlockedOnlyWhenNoOtherActiveTicketRemains() {
        groups.addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.plusDays(1).toString());
        tickets.createGroupTicket("TG02", "G01", TODAY.plusDays(2).toString());
        tickets.deleteGroupTicket("TG01");
        assertTrue(groups.isLocked(g01()));
        tickets.deleteGroupTicket("TG02");
        assertFalse(groups.isLocked(g01()));
    }

    @Test
    void ticketForTodayBlocksDeletion() {
        groups.addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertTrue(park.groups().findById("G01").isPresent());
    }

    @Test
    void deleteKeepsHistoricalTicketsWithoutGroup() {
        groups.addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        park.setDate(TODAY.plusDays(1).toString());
        groups.delete("G01");

        assertTrue(park.groups().findById("G01").isEmpty());
        GroupTicket ticket = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertNull(ticket.getGroup());
        assertEquals(List.of(ana), ticket.getHolders());

        restart();
        GroupTicket reloaded = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertNull(reloaded.getGroup());
        assertEquals("40.00", reloaded.getPrice().toString());
    }

    @Test
    void groupsSurviveRestart() {
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
        restart();
        Group g = g01();
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"),
                g.getMembers().stream().map(u -> u.getEmail()).toList());
        assertSame(park.users().findByEmail("ana@correo.com").orElseThrow(), g.getMembers().get(0));
    }
}
