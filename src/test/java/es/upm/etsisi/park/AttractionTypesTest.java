package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypesTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, ParkFixture.LUIS,
                "GROUP CREATE;G01;Trio", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL", "ATTRACTION CREATE;A02;Montana;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting;24;GROUP", "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
    }

    private List<String> inside(String attraction) {
        return f.park.findAttraction(attraction).getOccupants().stream().map(User::getEmail).toList();
    }

    private Pass pass(String id) {
        return f.park.findPass(id);
    }

    @Test
    void typesCreated() {
        assertEquals(AttractionType.GENERAL, f.park.findAttraction("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, f.park.findAttraction("A02").getType());
        assertEquals(AttractionType.GROUP, f.park.findAttraction("A03").getType());
        assertEquals(AttractionType.SPECIAL, f.park.findAttraction("A04").getType());
        f.run("ATTRACTION CREATE;A05;Noria;10");
        assertEquals(AttractionType.GENERAL, f.park.findAttraction("A05").getType());
        assertTrue(f.run("ATTRACTION CREATE;A06;Otra;10;VIP").contains("invalid attraction type VIP"));
        assertNull(f.park.findAttraction("A06"));
        assertTrue(f.run("ATTRACTION CREATE;A07;Otra;0;GROUP").contains("invalid capacity"));
    }

    @Test
    void acceptedTicketKinds() {
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01").contains("INDIVIDUAL attraction A02 does not accept group tickets"));
        assertTrue(f.run("ATTRACTION ENTER;A03;T001").contains("GROUP attraction A03 does not accept individual tickets"));
        assertTrue(f.run("ATTRACTION ENTER;A04;TG01").contains("SPECIAL attraction A04 does not accept group tickets"));
        assertTrue(inside("A02").isEmpty() && inside("A03").isEmpty() && inside("A04").isEmpty());
        assertEquals("ATTRACTION ENTERED: 1 USER(S)", f.run("ATTRACTION ENTER;A02;T001"));
        assertEquals("ATTRACTION ENTERED: 3 USER(S)", f.run("ATTRACTION ENTER;A03;TG01"));
        assertEquals("ATTRACTION ENTERED: 3 USER(S)", f.run("ATTRACTION ENTER;A01;TG01"));
    }

    @Test
    void individualSpeedEntryConsumesOnePass() {
        f.run("SPEEDPASS CREATE;SP01;T001;1", "SPEEDPASS CREATE;SP02;T001;1");
        assertEquals("ATTRACTION ENTERED: 1 USER(S). SPEEDPASS CONSUMED: SP01:1", f.run("ATTRACTION ENTER;A02;T001;SPEED"));
        assertTrue(pass("SP01:1").isConsumed());
        assertFalse(pass("SP02:1").isConsumed());
    }

    @Test
    void speedEntryWithoutPassChangesNothing() {
        assertTrue(f.run("ATTRACTION ENTER;A01;T001;SPEED").contains("has no unused SpeedPass"));
        assertTrue(inside("A01").isEmpty());
        assertTrue(f.run("ATTRACTION ENTER;A01;T001;FAST").contains("only SPEED is allowed"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        f.run("SPEEDPASS CREATE;SP03;TG01:12345678Z;1", "SPEEDPASS CREATE;SP04;TG01:87654321X;1");
        assertTrue(f.run("ATTRACTION ENTER;A03;TG01;SPEED").contains("TG01:11111111H has no unused SpeedPass"));
        assertTrue(inside("A03").isEmpty());
        assertFalse(pass("SP03:1").isConsumed());
        assertFalse(pass("SP04:1").isConsumed());

        f.run("SPEEDPASS CREATE;SP05;TG01:11111111H;10");
        assertTrue(f.run("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ATTRACTION ENTERED: 3 USER(S). SPEEDPASS CONSUMED:"));
        assertTrue(pass("SP03:1").isConsumed() && pass("SP04:1").isConsumed() && pass("SP05:1").isConsumed());
        assertFalse(pass("SP05:2").isConsumed());
    }

    @Test
    void groupSpeedEntryFailsOnCapacityWithoutConsuming() {
        f.run("ATTRACTION CREATE;A08;Mini;2;GENERAL", "SPEEDPASS CREATE;SP03;TG01:12345678Z;1",
                "SPEEDPASS CREATE;SP04;TG01:87654321X;1", "SPEEDPASS CREATE;SP05;TG01:11111111H;1");
        assertTrue(f.run("ATTRACTION ENTER;A08;TG01;SPEED").contains("not enough places"));
        assertFalse(pass("SP03:1").isConsumed());
    }

    @Test
    void specialEntryConsumesSpecialPass() throws IOException {
        f.run("SPECIALPASS CREATE;PE01;T001");
        assertTrue(f.run("ATTRACTION ENTER;A04;T001;SPEED").contains("does not accept SpeedPasses"));
        assertFalse(pass("PE01").isConsumed());
        assertEquals("ATTRACTION ENTERED: 1 USER(S). SPECIAL PASS CONSUMED: PE01", f.run("ATTRACTION ENTER;A04;T001"));
        assertTrue(pass("PE01").isConsumed());
        f.run("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(f.run("ATTRACTION ENTER;A04;T001").contains("PE01 has already been consumed"));
        f.restart();
        assertTrue(f.park.findPass("PE01").isConsumed());
    }

    @Test
    void specialEntryWithoutPassOrWithWrongDateIsRejected() {
        assertTrue(f.run("ATTRACTION ENTER;A04;T001").contains("has no Special Pass"));
        f.run("TICKET CREATE;T002;pedro@correo.com;2027-04-16", "SPECIALPASS CREATE;PE02;T002");
        assertTrue(f.run("ATTRACTION ENTER;A04;T002").contains("valid on 2027-04-16"));
        assertFalse(pass("PE02").isConsumed());
        assertTrue(inside("A04").isEmpty());
    }

    @Test
    void typesPersistAndLegacyLinesAreGeneral() throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), Files.readString(dir.resolve("attractions.txt"))
                + "A09;Legacy;5\nA10;Broken;5;VIP\n");
        f.restart();
        assertEquals(AttractionType.SPECIAL, f.park.findAttraction("A04").getType());
        assertEquals(AttractionType.GENERAL, f.park.findAttraction("A09").getType());
        assertNull(f.park.findAttraction("A10"));
        assertTrue(f.run("ATTRACTION STATUS;A03").contains("TYPE: GROUP"));
    }
}
