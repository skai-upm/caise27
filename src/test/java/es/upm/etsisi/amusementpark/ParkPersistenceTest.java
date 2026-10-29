package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParkPersistenceTest extends ParkFixture {

    @Test
    void fullStateSurvivesRestart() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        park.setCurrentPrice("45.50");
        park.createTicket("T001", ANA, "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-16");
        park.enterAttraction("A01", "T001");

        restart();

        Attraction a01 = park.findAttraction("A01");
        assertEquals("Dragon Rojo", a01.getName());
        assertEquals(20, a01.getCapacity());
        assertTrue(a01.getOccupants().isEmpty());

        Group g01 = park.findGroup("G01");
        assertEquals("Familia Garcia", g01.getName());
        assertEquals(List.of(user(ANA), user(PEDRO)), g01.getMembers());

        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertSame(user(ANA), t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());

        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertSame(g01, tg01.getGroup());
        assertEquals(List.of(user(ANA), user(PEDRO)), tg01.getUsers());
        assertEquals(new BigDecimal("91.00"), tg01.getPrice());
        assertEquals("2027-04-16", tg01.getDate().toString());
        assertNotNull(park.findTicket("TG01:87654321X"));
        assertTrue(park.isGroupLocked(g01));
    }

    @Test
    void historyOfDeletedUserAndGroupSurvivesRestart() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createTicket("T001", ANA, "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteGroup("G01");
        park.deleteUser(ANA, "Clave1!");

        restart();

        assertNull(user(ANA));
        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals(ANA, t001.getUser().getEmail());
        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertSame(t001.getUser(), tg01.getUsers().get(0));
        assertSame(user(PEDRO), tg01.getUsers().get(1));
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());
    }

    @Test
    void recreatedGroupWithSameIdIsNotLinkedToHistoricalTicket() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteGroup("G01");
        park.createGroup("G01", "Nueva");
        restart();
        assertNull(((GroupTicket) park.findTicket("TG01")).getGroup());
    }

    @Test
    void filesUseTextFormat() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", ANA);
        park.createTicket("T001", ANA, "2027-04-15");
        assertEquals(List.of("A01;Dragon Rojo;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        assertEquals(List.of("INDIVIDUAL;T001;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez"),
                Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A02;Bad;x", "junk"));
        Files.write(dir.resolve("tickets.txt"), List.of("INDIVIDUAL;T1;bad-date;40;0;1;a;b;c", "WHAT;x",
                "MEMBER;NOPE;1;a;b;c"));
        restart();
        assertNotNull(park.findAttraction("A01"));
        assertNull(park.findAttraction("A02"));
        assertTrue(park.getTickets().isEmpty());
    }
}
