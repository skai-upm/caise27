package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GroupAttraction;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialAttraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionTypeTest {

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 1));
        t.run(ParkTestSupport.CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUP CREATE;G01;Familia");
        t.addMembers("G01", 3);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "ATTRACTION CREATE;GEN;General;20",
                "ATTRACTION CREATE;IND;Individual;20;INDIVIDUAL",
                "ATTRACTION CREATE;GRP;Grupal;20;group",
                "ATTRACTION CREATE;SPC;Especial;20;SPECIAL",
                "SPECIALPASS CREATE;T001",
                "SYSTEM DATE;2026-10-15");
    }

    private IndividualTicket ticket(String id) {
        return t.tickets.getIndividual(id);
    }

    @Test
    void typesAreCreatedAndPersisted() throws IOException {
        assertEquals(AttractionType.GENERAL, t.attractions.get("GEN").getType());
        assertInstanceOf(GroupAttraction.class, t.attractions.get("GRP"));
        assertInstanceOf(SpecialAttraction.class, t.attractions.get("SPC"));
        assertTrue(Files.readString(dir.resolve("attractions.txt")).contains("SPC;Especial;20;SPECIAL"));
        assertEquals(AttractionType.INDIVIDUAL, t.restart(LocalDate.of(2026, 10, 15)).attractions.get("IND").getType());
        assertTrue(t.run("ATTRACTION CREATE;VIP;Vip;5;VIP").startsWith("ERROR Unknown attraction type"));
    }

    @ParameterizedTest
    @CsvSource({"GEN,T001,true", "GEN,TG01,true", "IND,T001,true", "IND,TG01,false", "GRP,T001,false",
            "GRP,TG01,true", "SPC,T001,true", "SPC,TG01,false"})
    void typeAndTicketKindMatrix(String attraction, String ticket, boolean admitted) {
        String out = t.run("ATTRACTION ENTER;" + attraction + ";" + ticket);
        assertEquals(admitted, out.startsWith("OK"), out);
        if (!admitted) {
            assertTrue(t.attractions.get(attraction).getPresent().isEmpty());
        }
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() {
        t.run("SYSTEM DATE;2026-10-14", "SPEEDPASS CREATE;T001;10", "SYSTEM DATE;2026-10-15");
        assertEquals("OK ATTRACTION ENTERED -> 1 (SPEEDPASS CONSUMED: T001-SP1)",
                t.run("ATTRACTION ENTER;GEN;T001;speed"));
        assertEquals(1, ticket("T001").getSpeedPasses().stream().filter(p -> p.isConsumed()).count());
        t.run("ATTRACTION EXIT;GEN;T001");
        assertEquals("OK ATTRACTION ENTERED -> 1 (SPEEDPASS CONSUMED: T001-SP2)",
                t.run("ATTRACTION ENTER;IND;T001;SPEED"));
    }

    @Test
    void groupSpeedEntryConsumesOnePerMemberAtomically() {
        t.run("SYSTEM DATE;2026-10-14", "SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1",
                "SYSTEM DATE;2026-10-15");
        assertTrue(t.run("ATTRACTION ENTER;GRP;TG01;SPEED").contains("TG01-3 has no unused SpeedPass"));
        assertTrue(t.attractions.get("GRP").getPresent().isEmpty());
        assertFalse(ticket("TG01-1").getSpeedPasses().get(0).isConsumed());

        t.run("SYSTEM DATE;2026-10-14", "SPEEDPASS CREATE;TG01-3;1", "SYSTEM DATE;2026-10-15");
        assertEquals("OK ATTRACTION ENTERED -> 3 (SPEEDPASS CONSUMED: TG01-1-SP1, TG01-2-SP1, TG01-3-SP1)",
                t.run("ATTRACTION ENTER;GRP;TG01;SPEED"));
    }

    @Test
    void failedSpeedEntryConsumesNothing() {
        t.run("SYSTEM DATE;2026-10-14", "SPEEDPASS CREATE;T001;1", "ATTRACTION CREATE;MINI;Mini;1",
                "SYSTEM DATE;2026-10-15", "ATTRACTION ENTER;MINI;TG01-1");
        assertTrue(t.run("ATTRACTION ENTER;MINI;T001;SPEED").contains("Not enough capacity"));
        t.run("SYSTEM DATE;2026-10-16");
        assertTrue(t.run("ATTRACTION ENTER;GEN;T001;SPEED").startsWith("ERROR"));
        assertFalse(ticket("T001").getSpeedPasses().get(0).isConsumed());
        assertTrue(t.run("ATTRACTION ENTER;GEN;T001;FAST").startsWith("ERROR Unknown entry option"));
    }

    @Test
    void specialAttractionConsumesTheSpecialPass() {
        assertEquals("OK ATTRACTION ENTERED -> 1 (SPECIAL PASS CONSUMED: T001-SPECIAL)",
                t.run("ATTRACTION ENTER;SPC;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        t.run("ATTRACTION EXIT;SPC;T001");
        assertTrue(t.run("ATTRACTION ENTER;SPC;T001").contains("already used"));
        assertTrue(t.run("ATTRACTION ENTER;SPC;TG01-1").contains("has no Special Pass"));
        assertTrue(t.attractions.get("SPC").getPresent().isEmpty());
    }

    @Test
    void speedIsRejectedAtSpecialAttractions() {
        t.run("SYSTEM DATE;2026-10-14", "SPEEDPASS CREATE;T001;1", "SYSTEM DATE;2026-10-15");
        assertTrue(t.run("ATTRACTION ENTER;SPC;T001;SPEED").contains("SPEED is not accepted"));
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertFalse(ticket("T001").getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void statusShowsTheType() {
        assertTrue(t.output("ATTRACTION STATUS;SPC").contains("Type: SPECIAL"));
    }
}
