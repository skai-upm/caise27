package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.model.Pass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassEntryTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        p.process(TestData.CREATE_PEDRO);
        p.process(TestData.CREATE_LUIS);
        p.process("GROUP CREATE;G01;Amigos");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("GROUP ADD;G01;luis@correo.com");
        p.process("SYSTEM DATE;2026-10-15");
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("TICKET CREATE;T002;ana@correo.com;2026-10-15");
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        p.process("ATTRACTION CREATE;A01;Tornado;20");
        p.process("ATTRACTION CREATE;A02;Coches;20;GROUP");
        p.process("ATTRACTION CREATE;A03;Noria;20;INDIVIDUAL");
        p.process("ATTRACTION CREATE;A04;Vip;20;SPECIAL");
        p.process("ATTRACTION CREATE;A05;Mini;1");
    }

    private Pass pass(String id) {
        return PassController.getInstance().find(id).orElseThrow();
    }

    private int present(String attraction) {
        return AttractionController.getInstance().get(attraction).getPresent().size();
    }

    @Test
    void normalEntryConsumesNothing() {
        p.process("SPEEDPASS CREATE;S1;T001;1");
        assertEquals("OK: 1 user(s) entered attraction A01 (19 places available)", p.process("ATTRACTION ENTER;A01;T001"));
        assertFalse(pass("S1-1").isConsumed());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        p.process("SPEEDPASS CREATE;S1;T001;10");
        assertEquals("OK: 1 user(s) entered attraction A03 (19 places available); consumed SpeedPass S1-1",
                p.process("ATTRACTION ENTER;A03;T001;speed"));
        assertTrue(pass("S1-1").isConsumed());
        assertFalse(pass("S1-2").isConsumed());
        p.process("ATTRACTION EXIT;A03;T001");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001;SPEED").endsWith("consumed SpeedPass S1-2"));
    }

    @Test
    void speedEntryWithoutSpeedPassFails() {
        assertEquals("ERROR: Ticket T002 has no unused SpeedPass", p.process("ATTRACTION ENTER;A01;T002;SPEED"));
        assertEquals(0, present("A01"));
    }

    @Test
    void groupSpeedEntryIsAllOrNothing() {
        p.process("SPEEDPASS CREATE;S1;TG01-1;1");
        assertEquals("ERROR: Ticket TG01-2 has no unused SpeedPass", p.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertEquals(0, present("A02"));
        assertFalse(pass("S1-1").isConsumed());
        p.process("SPEEDPASS CREATE;S2;TG01-2;1");
        assertEquals("OK: 2 user(s) entered attraction A02 (18 places available); consumed SpeedPasses S1-1, S2-1",
                p.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertTrue(pass("S1-1").isConsumed() && pass("S2-1").isConsumed());
    }

    @Test
    void capacityFailureConsumesNothing() {
        p.process("SPEEDPASS CREATE;S1;TG01-1;1");
        p.process("SPEEDPASS CREATE;S2;TG01-2;1");
        assertTrue(p.process("ATTRACTION ENTER;A05;TG01;SPEED").startsWith("ERROR: Not enough capacity"));
        assertEquals(0, present("A05"));
        assertFalse(pass("S1-1").isConsumed() || pass("S2-1").isConsumed());
    }

    @Test
    void specialEntryConsumesTheSpecialPassAutomatically() {
        p.process("SPECIALPASS CREATE;SP1;T001");
        assertEquals("OK: 1 user(s) entered attraction A04 (19 places available); consumed Special Pass SP1",
                p.process("ATTRACTION ENTER;A04;T001"));
        assertTrue(pass("SP1").isConsumed());
        p.process("ATTRACTION EXIT;A04;T001");
        assertEquals("ERROR: Ticket T001 has no unused Special Pass", p.process("ATTRACTION ENTER;A04;T001"));
        assertEquals("ERROR: Ticket T002 has no unused Special Pass", p.process("ATTRACTION ENTER;A04;T002"));
    }

    @Test
    void speedIsRejectedAtSpecialAttractions() {
        p.process("SPECIALPASS CREATE;SP1;T001");
        p.process("SPEEDPASS CREATE;S1;T001;1");
        assertEquals("ERROR: SPEED is not accepted at SPECIAL attraction A04", p.process("ATTRACTION ENTER;A04;T001;SPEED"));
        assertFalse(pass("SP1").isConsumed() || pass("S1-1").isConsumed());
        assertEquals(0, present("A04"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION ENTER;A01;T001;FAST", "ATTRACTION ENTER;A02;T001;SPEED",
        "ATTRACTION ENTER;A03;TG01;SPEED", "ATTRACTION ENTER;A01;T001;SPEED;x"})
    void otherFailuresConsumeNothing(String command) {
        p.process("SPEEDPASS CREATE;S1;T001;1");
        p.process("SPEEDPASS CREATE;S2;TG01-1;1");
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertFalse(pass("S1-1").isConsumed() || pass("S2-1").isConsumed());
    }

    @Test
    void wrongDateConsumesNothing() {
        p.process("TICKET CREATE;T003;ana@correo.com;2026-10-16");
        p.process("SPEEDPASS CREATE;S1;T003;1");
        assertTrue(p.process("ATTRACTION ENTER;A01;T003;SPEED").startsWith("ERROR: Ticket T003 is not valid"));
        assertFalse(pass("S1-1").isConsumed());
    }

    @Test
    void consumptionIsPersistedAndNotRefundable() throws IOException {
        p.process("TICKET CREATE;T009;pedro@correo.com;2026-10-15");
        p.process("SPEEDPASS CREATE;S1;T001;1");
        p.process("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(List.of("S1-1;SPEED;T001;8.00;true"), Files.readAllLines(dir.resolve("passes.txt")));
        CommandProcessor restarted = App.createProcessor(dir);
        restarted.process("SYSTEM DATE;2026-10-01");
        assertTrue(pass("S1-1").isConsumed());
        assertTrue(restarted.process("SPEEDPASS DELETE;S1-1").startsWith("ERROR:"));
    }
}
