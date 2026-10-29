package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        park.enterAttraction("A01", "T001", false).users();

        restart();

        Attraction<?> a01 = park.findAttraction("A01");
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
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
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

    @Test
    void attractionTypesAndPassesSurviveRestart() throws Exception {
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createTicket("T001", ANA, "2027-04-15");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", PEDRO);
        park.createGroupTicket("TG01", "G01", "2027-04-16");
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP01", "T001", "10");
        park.buySpeedPasses("SP02", "TG01:87654321X", "1");
        park.enterAttraction("A04", "T001", false);

        restart();

        PassController passes = PassController.getInstance();
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
        assertEquals(12, passes.getPasses().size());
        Pass special = passes.findPass("PE01");
        assertInstanceOf(SpecialPass.class, special);
        assertTrue(special.isConsumed());
        assertSame(park.findTicket("T001"), special.getTicket());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        Pass speed = passes.findPass("SP01:10");
        assertInstanceOf(SpeedPass.class, speed);
        assertFalse(speed.isConsumed());
        assertEquals(new BigDecimal("8.00"), speed.getPrice());
        assertSame(park.findTicket("TG01:87654321X"), passes.findPass("SP02:1").getTicket());
    }

    @Test
    void deliveryTwoAttractionLinesLoadAsGeneral() throws Exception {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A02;Bad;3;VIP"));
        restart();
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertNull(park.findAttraction("A02"));
    }

    @Test
    void passesFileFormat() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpecialPass("PE01", "T001");
        assertEquals(List.of("SPEED;SP01:1;T001;8.00;false", "SPECIAL;PE01;T001;40.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));
    }
}
