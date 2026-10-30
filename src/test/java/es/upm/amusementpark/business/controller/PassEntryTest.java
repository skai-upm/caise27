package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassEntryTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        s.run("ATTRACTION CREATE;GEN;General;20", "ATTRACTION CREATE;IND;Individual;20;INDIVIDUAL",
                "ATTRACTION CREATE;GRP;Grupo;20;GROUP", "ATTRACTION CREATE;SPE;Especial;20;SPECIAL");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().findById(id);
    }

    private long consumedCount(String ticketId) {
        return ticket(ticketId).getPasses().stream().filter(Pass::isConsumed).count();
    }

    private int present(String attractionId) {
        return AttractionController.getInstance().findById(attractionId).getPresent().size();
    }

    @Test
    void normalEntryStillWorksAndConsumesNothing() {
        s.run("SPEEDPASS CREATE;T001;1", "SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered GEN", s.run("ATTRACTION ENTER;GEN;T001"));
        assertEquals(0, consumedCount("T001"));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        s.run("SPEEDPASS CREATE;T001;10", "SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered IND; consumed SpeedPass T001-SP1", s.run("ATTRACTION ENTER;IND;T001;SPEED"));
        assertEquals(1, consumedCount("T001"));
        s.run("ATTRACTION EXIT;IND;T001");
        assertTrue(s.run("ATTRACTION ENTER;GEN;T001;SPEED").endsWith("consumed SpeedPass T001-SP2"));
        List<SpeedPass> passes = ticket("T001").getSpeedPasses();
        assertEquals(10, passes.size());
        assertTrue(passes.get(0).isConsumed());
        assertTrue(passes.get(1).isConsumed());
        assertFalse(passes.get(2).isConsumed());
    }

    @Test
    void speedWithoutPassIsRejected() {
        s.run("SYSTEM DATE;2026-10-15");
        assertTrue(s.run("ATTRACTION ENTER;GEN;T001;SPEED").startsWith("ERROR"));
        assertEquals(0, present("GEN"));
        assertTrue(s.run("ATTRACTION ENTER;GEN;T001;FAST").startsWith("ERROR"));
    }

    @Test
    void groupSpeedEntryConsumesOnePerMember() {
        s.run("SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1", "SPEEDPASS CREATE;TG01-3;10",
                "SYSTEM DATE;2026-10-15");
        String out = s.run("ATTRACTION ENTER;GRP;TG01;SPEED");
        assertTrue(out.startsWith("OK: 3 user(s) entered GRP"), out);
        assertTrue(out.contains("TG01-1-SP1") && out.contains("TG01-2-SP1") && out.contains("TG01-3-SP1"));
        assertEquals(1, consumedCount("TG01-1"));
        assertEquals(1, consumedCount("TG01-3"));
    }

    @Test
    void groupSpeedEntryIsAtomicWhenOneMemberLacksAPass() {
        s.run("SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1", "SYSTEM DATE;2026-10-15");
        assertTrue(s.run("ATTRACTION ENTER;GEN;TG01;SPEED").startsWith("ERROR"));
        assertEquals(0, present("GEN"));
        assertEquals(0, consumedCount("TG01-1"));
        assertEquals(0, consumedCount("TG01-2"));
    }

    @Test
    void failedSpeedEntryForCapacityConsumesNothing() {
        s.run("SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1", "SPEEDPASS CREATE;TG01-3;1",
                "ATTRACTION CREATE;MINI;Mini;2", "SYSTEM DATE;2026-10-15");
        assertTrue(s.run("ATTRACTION ENTER;MINI;TG01;SPEED").startsWith("ERROR"));
        assertEquals(0, consumedCount("TG01-1"));
        assertEquals(0, present("MINI"));
    }

    @Test
    void specialAttractionConsumesSpecialPass() {
        s.run("SPECIALPASS CREATE;T001", "SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered SPE; consumed Special Pass T001-SPECIAL", s.run("ATTRACTION ENTER;SPE;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        s.run("ATTRACTION EXIT;SPE;T001");
        assertTrue(s.run("ATTRACTION ENTER;SPE;T001").startsWith("ERROR"));
        assertEquals(0, present("SPE"));
    }

    @Test
    void specialAttractionRequiresPassAndRejectsSpeed() {
        s.run("SPEEDPASS CREATE;T001;1", "SYSTEM DATE;2026-10-15");
        assertTrue(s.run("ATTRACTION ENTER;SPE;T001").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-01", "SPECIALPASS CREATE;T001", "SYSTEM DATE;2026-10-15");
        assertTrue(s.run("ATTRACTION ENTER;SPE;T001;SPEED").startsWith("ERROR"));
        assertEquals(0, consumedCount("T001"));
        assertEquals(0, present("SPE"));
    }
}
