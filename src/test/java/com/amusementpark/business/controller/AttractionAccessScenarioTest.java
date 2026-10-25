package com.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.TestSupport;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.SpeedPass;
import com.amusementpark.presentation.Bootstrap;
import com.amusementpark.presentation.CommandProcessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AttractionAccessScenarioTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws Exception {
        start();
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("ATTRACTION CREATE;GEN;General;10");
        run("ATTRACTION CREATE;IND;Individual;10;INDIVIDUAL");
        run("ATTRACTION CREATE;GRP;Grupo;10;GROUP");
        run("ATTRACTION CREATE;SPE;Especial;10;SPECIAL");
    }

    private void start() throws Exception {
        buffer = new ByteArrayOutputStream();
        processor = Bootstrap.initialize(dir, TODAY, TestSupport.FAST_HASHER,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private void assertOk(String command) {
        String out = run(command);
        assertTrue(out.startsWith("OK"), command + " -> " + out);
    }

    private void assertError(String command) {
        String out = run(command);
        assertTrue(out.startsWith("ERROR"), command + " -> " + out);
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().findTicket(id);
    }

    private long unusedSpeed(String ticketId) {
        return ticket(ticketId).getSpeedPasses().stream().filter(p -> !p.isConsumed()).count();
    }

    @Test
    public void typesAndDefault() {
        AttractionController attractions = AttractionController.getInstance();
        assertEquals(AttractionType.GENERAL, attractions.findAttraction("GEN").getType());
        assertEquals(AttractionType.SPECIAL, attractions.findAttraction("SPE").getType());
        assertError("ATTRACTION CREATE;X;X;10;OTHER");
        assertError("ATTRACTION CREATE;X;X;10;GROUP;EXTRA");
    }

    @Test
    public void ticketKindsPerType() {
        assertOk("ATTRACTION ENTER;GEN;T001");
        assertOk("ATTRACTION EXIT;GEN;T001");
        assertOk("ATTRACTION ENTER;GEN;TG01");
        assertOk("ATTRACTION ENTER;IND;T001");
        assertError("ATTRACTION ENTER;IND;TG01");
        assertError("ATTRACTION ENTER;GRP;T001");
        assertOk("ATTRACTION ENTER;GRP;TG01");
        assertError("ATTRACTION ENTER;SPE;TG01");
    }

    @Test
    public void individualSpeedEntryConsumesOne() {
        run("SPEEDPASS CREATE;T001;1");
        run("SPEEDPASS CREATE;T001;1");
        assertEquals("OK: 1 user(s) entered IND; SpeedPass consumed: T001-SP1", run("ATTRACTION ENTER;IND;T001;SPEED"));
        assertEquals(1, unusedSpeed("T001"));
        assertError("ATTRACTION ENTER;IND;T001;FAST");
        run("ATTRACTION EXIT;IND;T001");
        assertOk("ATTRACTION ENTER;GEN;T001;SPEED");
        assertEquals(0, unusedSpeed("T001"));
        run("ATTRACTION EXIT;GEN;T001");
        assertError("ATTRACTION ENTER;GEN;T001;SPEED");
    }

    @Test
    public void groupSpeedEntryIsAtomic() {
        run("SPEEDPASS CREATE;TG01-1;1");
        assertError("ATTRACTION ENTER;GRP;TG01;SPEED");
        assertEquals(1, unusedSpeed("TG01-1"));
        assertTrue(run("ATTRACTION STATUS;GRP").contains("Present: 0"));

        run("SPEEDPASS CREATE;TG01-2;1");
        String out = run("ATTRACTION ENTER;GRP;TG01;SPEED");
        assertEquals("OK: 2 user(s) entered GRP; SpeedPass consumed: TG01-1-SP1, TG01-2-SP1", out);
        assertEquals(0, unusedSpeed("TG01-1"));
        assertEquals(0, unusedSpeed("TG01-2"));
    }

    @Test
    public void failedEntryConsumesNothing() {
        run("ATTRACTION CREATE;TINY;Tiny;1");
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPEEDPASS CREATE;TG01-2;1");
        assertError("ATTRACTION ENTER;TINY;TG01;SPEED");
        assertEquals(1, unusedSpeed("TG01-1"));
        assertEquals(1, unusedSpeed("TG01-2"));
        run("SYSTEM DATE;2026-10-16");
        assertError("ATTRACTION ENTER;GEN;TG01;SPEED");
        assertEquals(1, unusedSpeed("TG01-1"));
    }

    @Test
    public void specialAttractionConsumesSpecialPass() {
        assertError("ATTRACTION ENTER;SPE;T001");
        run("SPECIALPASS CREATE;T001");
        run("SPEEDPASS CREATE;T001;1");
        assertError("ATTRACTION ENTER;SPE;T001;SPEED");
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertEquals(1, unusedSpeed("T001"));
        assertEquals("OK: 1 user(s) entered SPE; Special Pass consumed: T001-SPECIAL", run("ATTRACTION ENTER;SPE;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        run("ATTRACTION EXIT;SPE;T001");
        assertError("ATTRACTION ENTER;SPE;T001");
    }

    @Test
    public void consumptionAndTypesSurviveRestart() throws Exception {
        run("SPECIALPASS CREATE;TG01-1");
        run("SPEEDPASS CREATE;T001;1");
        run("ATTRACTION ENTER;SPE;TG01-1");
        run("ATTRACTION ENTER;GEN;T001;SPEED");
        start();
        assertTrue(ticket("TG01-1").getSpecialPass().isConsumed());
        SpeedPass speedPass = ticket("T001").getSpeedPasses().get(0);
        assertTrue(speedPass.isConsumed());
        assertEquals(AttractionType.SPECIAL, AttractionController.getInstance().findAttraction("SPE").getType());
        assertTrue(run("ATTRACTION STATUS;SPE").contains("Present: 0"));
    }
}
