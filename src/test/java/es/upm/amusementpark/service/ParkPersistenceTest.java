package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.ticket.GroupTicket;
import es.upm.amusementpark.model.ticket.IndividualTicket;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ParkPersistenceTest extends ParkTestSupport {

    @Test
    void reconstructsGroupsTicketsAndAttractions() throws Exception {
        createAna();
        createPedro();
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.setCurrentPrice("45.50");
        park.createTicket("T001", "ana@correo.com", TOMORROW_TEXT);
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.enterAttraction("A01", "TG01");

        restart();

        User ana = park.findUser("ana@correo.com");
        User pedro = park.findUser("pedro@correo.com");
        Group group = park.findGroup("G01");
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.hasMember(ana) && group.hasMember(pedro));

        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertSame(ana, t001.getUser());
        assertEquals(LocalDate.of(2027, 4, 16), t001.getDate());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());

        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertSame(group, tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertSame(tg01.getComposition().get(0), park.findTicket("TG01:12345678Z"));
        assertSame(pedro, ((IndividualTicket) park.findTicket("TG01:87654321X")).getUser());
        assertEquals(new BigDecimal("91.00"), tg01.getPrice());
        assertTrue(park.isLocked(group));

        assertEquals(20, park.findAttraction("A01").getCapacity());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurvive() throws Exception {
        createAna();
        createPedro();
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);
        park.deleteGroup("G01");
        park.deleteUser("ana@correo.com", "Clave1!");

        restart();

        assertNull(park.findUser("ana@correo.com"));
        assertNull(park.findGroup("G01"));
        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertNotNull(t001);
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        assertFalse(park.isActiveUser(t001.getUser()));
        assertEquals(new BigDecimal("40.00"), t001.getPrice());

        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertSame(t001.getUser(), ((IndividualTicket) park.findTicket("TG01:12345678Z")).getUser());
        assertSame(park.findUser("pedro@correo.com"),
                ((IndividualTicket) park.findTicket("TG01:87654321X")).getUser());
    }

    @Test
    void recreatedGroupDoesNotAdoptDetachedTickets() throws Exception {
        createAna();
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);
        park.deleteGroup("G01");
        park.createGroup("G01", "Otra familia");

        restart();

        assertNull(((GroupTicket) park.findTicket("TG01")).getGroup());
    }

    @Test
    void writesSeparateFiles() throws Exception {
        createAna();
        park.createGroup("G01", "Familia");
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        assertTrue(Files.readString(dir.resolve("users.txt")).contains("ana@correo.com"));
        assertEquals("G01;Familia;", Files.readString(dir.resolve("groups.txt")).strip());
        assertEquals("A01;Dragon Rojo;20", Files.readString(dir.resolve("attractions.txt")).strip());
        assertTrue(Files.readString(dir.resolve("tickets.txt")).startsWith("I;T001;2027-04-15;40.00;0;;A;12345678Z"));
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        Files.writeString(dir.resolve("groups.txt"), "broken\nG01;Familia;99999999R\n");
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon;x\nA02;Torre;12\n");
        Files.writeString(dir.resolve("tickets.txt"), "X;1\nI;T9;2027-04-15;40.00;0;TGX;A;1;a;b;c\n");
        restart();
        assertTrue(park.findGroup("G01").isEmpty());
        assertEquals(12, park.findAttraction("A02").getCapacity());
        assertTrue(park.getTickets().isEmpty());
    }
}
