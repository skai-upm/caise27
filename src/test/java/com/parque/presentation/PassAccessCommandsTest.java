package com.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.ApplicationContext;
import com.parque.TestPark;
import com.parque.business.controller.ParkController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassAccessCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor p;

    private void ok(String c) {
        String r = p.process(c);
        assertTrue(r.startsWith("OK"), c + " -> " + r);
    }

    private void error(String c) {
        String r = p.process(c);
        assertTrue(r.startsWith("ERROR: "), c + " -> " + r);
    }

    private List<String> consumedIds() throws Exception {
        return Files.readAllLines(dir.resolve("passes.txt")).stream().filter(l -> l.endsWith(";true"))
                .map(l -> l.split(";")[1]).toList();
    }

    private int present(String attraction) {
        return p.getController().attraction(attraction).getPresent().size();
    }

    @BeforeEach
    void setUp() throws Exception {
        ParkController c = TestPark.start(dir);
        p = new CommandProcessor(c);
        ok("SYSTEM DATE;2026-10-01");
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        ok("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        ok("USER CREATE;Marta;Ruiz;22222222J;marta@correo.com;Marta4%;645678901;2222333344445555");
        ok("GROUP CREATE;G01;Amigos");
        ok("GROUP ADD;G01;pedro@correo.com");
        ok("GROUP ADD;G01;luis@correo.com");
        ok("GROUP ADD;G01;marta@correo.com");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION CREATE;A02;Familiar;30;GROUP");
        ok("ATTRACTION CREATE;A03;Mini;2;GROUP");
        ok("ATTRACTION CREATE;A04;Magia;5;SPECIAL");
        ok("ATTRACTION CREATE;A05;Solo;5;INDIVIDUAL");
    }

    @AfterEach
    void tearDown() {
        ApplicationContext.stop();
    }

    @Test
    void normalEntryConsumesNothing() throws Exception {
        ok("SPEEDPASS CREATE;P02;T001;10");
        ok("SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered attraction A01", p.process("ATTRACTION ENTER;A01;T001"));
        assertEquals(List.of(), consumedIds());
        error("ATTRACTION ENTER;A05;T001;FAST");
    }

    @Test
    void individualSpeedConsumesExactlyOne() throws Exception {
        ok("SPEEDPASS CREATE;P02;T001;10");
        ok("SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered attraction A01; SpeedPass consumed: P02-1",
                p.process("ATTRACTION ENTER;A01;T001;SPEED"));
        assertEquals(List.of("P02-1"), consumedIds());
        ok("ATTRACTION EXIT;A01;T001");
        assertEquals("OK: 1 user(s) entered attraction A05; SpeedPass consumed: P02-2",
                p.process("ATTRACTION ENTER;A05;T001;SPEED"));
    }

    @Test
    void individualWithoutSpeedPassRejected() throws Exception {
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(0, present("A01"));
    }

    @Test
    void groupSpeedAllOrNothing() throws Exception {
        ok("SPEEDPASS CREATE;P01;TG01-1;1");
        ok("SPEEDPASS CREATE;P02;TG01-2;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A02;TG01;SPEED");
        assertEquals(0, present("A02"));
        assertEquals(List.of(), consumedIds());
        ok("SYSTEM DATE;2026-10-01");
        ok("SPEEDPASS CREATE;P03;TG01-3;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A03;TG01;SPEED");
        assertEquals(List.of(), consumedIds(), "capacity failure consumes nothing");
        assertEquals("OK: 3 user(s) entered attraction A02; SpeedPasses consumed: P01-1, P02-1, P03-1",
                p.process("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertEquals(List.of("P01-1", "P02-1", "P03-1"), consumedIds());
    }

    @Test
    void specialEntryUsesSpecialPass() throws Exception {
        ok("SPECIALPASS CREATE;S01;T001");
        ok("SPEEDPASS CREATE;P01;T001;1");
        ok("SYSTEM DATE;2026-10-15");
        error("ATTRACTION ENTER;A04;T001;SPEED");
        assertEquals(List.of(), consumedIds(), "SPEED at SPECIAL changes nothing");
        error("ATTRACTION ENTER;A04;TG01");
        error("ATTRACTION ENTER;A04;TG01-1");
        assertEquals("OK: 1 user(s) entered attraction A04; Special Pass consumed: S01",
                p.process("ATTRACTION ENTER;A04;T001"));
        assertEquals(List.of("S01"), consumedIds());
        ok("ATTRACTION EXIT;A04;T001");
        error("ATTRACTION ENTER;A04;T001");
        assertEquals(0, present("A04"));
    }

    @Test
    void specialRejectsWhenOtherConditionsFail() throws Exception {
        ok("SPECIALPASS CREATE;S01;T001");
        error("ATTRACTION ENTER;A04;T001");
        assertEquals(List.of(), consumedIds(), "wrong date consumes nothing");
    }

    @Test
    void consumptionSurvivesRestartAndBlocksRefund() throws Exception {
        ok("SPEEDPASS CREATE;P02;T001;10");
        ok("SYSTEM DATE;2026-10-15");
        ok("ATTRACTION ENTER;A01;T001;SPEED");
        ApplicationContext.stop();
        p = new CommandProcessor(TestPark.start(dir));
        ok("SYSTEM DATE;2026-10-01");
        error("SPEEDPASS DELETE;P02-1");
        ok("SPEEDPASS DELETE;P02-2");
        assertEquals(List.of("P02-1"), consumedIds());
        assertFalse(p.process("HELP").contains("ATTRACTION ENTER;Identificador;Ticket\n"));
        assertTrue(p.process("HELP").contains("ATTRACTION ENTER;Identificador;Ticket[;SPEED]"));
    }
}
