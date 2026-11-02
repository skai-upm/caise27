package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.Pass;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassEntryTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.group("G01", 2, 50000000);
        f.ok("SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20",
                "ATTRACTION CREATE;A02;Solo;20;INDIVIDUAL", "ATTRACTION CREATE;A03;Familiar;20;GROUP",
                "ATTRACTION CREATE;A04;Magica;20;SPECIAL");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().find(id);
    }

    private long used(String ticketId) {
        return ticket(ticketId).getPasses().stream().filter(Pass::isConsumed).count();
    }

    private int visitors(String attractionId) {
        return AttractionController.getInstance().find(attractionId).getVisitors().size();
    }

    @Test
    void normalEntryKeepsWorkingAndConsumesNothing() {
        f.ok("SPEEDPASS CREATE;T001;1");
        assertEquals("OK: 1 user(s) entered A01", f.run("ATTRACTION ENTER;A01;T001"));
        assertEquals(0, used("T001"));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        f.ok("SPEEDPASS CREATE;T001;10");
        assertEquals("OK: 1 user(s) entered A02 (consumed: SpeedPass T001-S1)", f.run("ATTRACTION ENTER;A02;T001;SPEED"));
        assertEquals(1, used("T001"));
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
        f.ok("ATTRACTION EXIT;A02;T001");
        assertTrue(f.run("ATTRACTION ENTER;A01;T001;SPEED").contains("T001-S2"));
        assertEquals(2, used("T001"));
    }

    @Test
    void speedWithoutPassIsRejectedWithoutChanges() {
        assertTrue(f.run("ATTRACTION ENTER;A01;T001;SPEED").startsWith("ERROR:"));
        assertEquals(0, visitors("A01"));
        assertTrue(f.run("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR:"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        f.ok("SPEEDPASS CREATE;TG01-1;1");
        assertTrue(f.run("ATTRACTION ENTER;A01;TG01;SPEED").startsWith("ERROR:"));
        assertEquals(0, visitors("A01"));
        assertEquals(0, used("TG01-1"), "no pass consumed when a member lacks one");
    }

    @Test
    void groupSpeedEntryConsumesOnePerMember() {
        f.ok("SPEEDPASS CREATE;TG01-1;10", "SPEEDPASS CREATE;TG01-2;1");
        String out = f.run("ATTRACTION ENTER;A03;TG01;SPEED");
        assertEquals("OK: 2 user(s) entered A03 (consumed: SpeedPass TG01-1-S1, SpeedPass TG01-2-S1)", out);
        assertEquals(1, used("TG01-1"));
        assertEquals(1, used("TG01-2"));
    }

    @Test
    void failedCapacityConsumesNothing() {
        f.ok("ATTRACTION CREATE;A05;Mini;1", "SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1");
        assertTrue(f.run("ATTRACTION ENTER;A05;TG01;SPEED").startsWith("ERROR:"));
        assertEquals(0, used("TG01-1") + used("TG01-2"));
    }

    @Test
    void groupSpeedAtIndividualAttractionIsRejected() {
        f.ok("SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1");
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01;SPEED").startsWith("ERROR:"));
        assertEquals(0, used("TG01-1") + used("TG01-2"));
    }

    @Test
    void specialAttractionConsumesSpecialPassAutomatically() {
        f.ok("SPECIALPASS CREATE;T001");
        assertEquals("OK: 1 user(s) entered A04 (consumed: Special Pass T001-SPECIAL)", f.run("ATTRACTION ENTER;A04;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        f.ok("ATTRACTION EXIT;A04;T001");
        assertTrue(f.run("ATTRACTION ENTER;A04;T001").startsWith("ERROR:"), "Special Pass already used");
    }

    @Test
    void specialAttractionRequiresSpecialPassAndRejectsSpeed() {
        f.ok("SPEEDPASS CREATE;T001;1");
        assertTrue(f.run("ATTRACTION ENTER;A04;T001").startsWith("ERROR:"), "no Special Pass");
        f.ok("SPECIALPASS CREATE;T001");
        assertTrue(f.run("ATTRACTION ENTER;A04;T001;SPEED").startsWith("ERROR:"));
        assertEquals(0, used("T001"));
        assertEquals(0, visitors("A04"));
    }

    @Test
    void specialPassOnlyUsedAfterDateAndCapacityChecks() {
        f.ok("TICKET CREATE;T002;ana@correo.com;2026-10-16", "SPECIALPASS CREATE;T002");
        assertTrue(f.run("ATTRACTION ENTER;A04;T002").startsWith("ERROR:"));
        assertFalse(ticket("T002").getSpecialPass().isConsumed());
    }
}
