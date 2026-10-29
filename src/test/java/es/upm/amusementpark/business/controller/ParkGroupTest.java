package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.ParkException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.ticket.GroupTicket;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkGroupTest extends ParkTestSupport {

    @BeforeEach
    void users() throws Exception {
        createAna();
        createPedro();
        park.createGroup("G01", "Familia Garcia");
    }

    @Test
    void createsEmptyGroup() {
        Group group = park.findGroup("G01");
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.isEmpty());
    }

    @Test
    void rejectsDuplicateOrEmptyIdentifier() {
        assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
        assertThrows(ParkException.class, () -> park.createGroup("", "Sin id"));
    }

    @Test
    void addsAndRemovesMembers() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        assertEquals(2, park.findGroup("G01").getMembers().size());

        park.removeFromGroup("G01", "ana@correo.com");
        assertEquals(List.of(park.findUser("pedro@correo.com")), park.findGroup("G01").getMembers());
    }

    @Test
    void rejectsDuplicateMember() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "ana@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void rejectsUnknownGroupOrUser() {
        assertThrows(ParkException.class, () -> park.addToGroup("G99", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertTrue(park.findGroup("G01").isEmpty());
    }

    @Test
    void rejectsRemovingNonMember() {
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
    }

    @Test
    void activeGroupTicketLocksGroup() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);

        assertTrue(park.isLocked(park.findGroup("G01")));
        assertThrows(ParkException.class, () -> park.addToGroup("G01", "pedro@correo.com"));
        assertThrows(ParkException.class, () -> park.removeFromGroup("G01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void historicalTicketDoesNotLock() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);

        assertFalse(park.isLocked(park.findGroup("G01")));
        park.addToGroup("G01", "pedro@correo.com");
        park.removeFromGroup("G01", "ana@correo.com");
    }

    @Test
    void deletingFutureTicketUnlocks() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TOMORROW_TEXT);
        park.createGroupTicket("TG02", "G01", "2027-04-17");
        park.deleteGroupTicket("TG01");
        assertTrue(park.isLocked(park.findGroup("G01")));
        park.deleteGroupTicket("TG02");
        assertFalse(park.isLocked(park.findGroup("G01")));
        park.addToGroup("G01", "pedro@correo.com");
    }

    @Test
    void deleteGroupKeepsHistoricalTicketsWithoutGroup() throws Exception {
        park.addToGroup("G01", "ana@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);

        park.deleteGroup("G01");

        assertNull(park.findGroup("G01"));
        assertSame(ticket, park.findTicket("TG01"));
        assertNull(ticket.getGroup());
        assertEquals(1, ticket.getComposition().size());
        park.createGroup("G01", "Nuevo");
        assertTrue(park.groupTicketsOf(park.findGroup("G01")).isEmpty());
    }

    @Test
    void groupHasNoTicketReferences() {
        for (Field field : Group.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().contains("ticket"), field.getName());
            assertFalse(field.getGenericType().getTypeName().contains("ticket"), field.getName());
        }
    }
}
