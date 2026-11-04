package com.parque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delivery 3 state across executions, in normal and --fly mode.
 */
class F3PersistenceTest {
    static final String SETUP = String.join("\n",
            "SYSTEM DATE;2026-10-01",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15",
            "SPEEDPASS CREATE;P02;T001;10",
            "SPECIALPASS CREATE;S01;T001",
            "ATTRACTION CREATE;A01;Tornado;20",
            "ATTRACTION CREATE;A04;Magia;5;SPECIAL",
            "SYSTEM DATE;2026-10-15",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION EXIT;A04;T001",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "EXIT") + "\n";

    @TempDir
    Path base;

    private List<String> run(String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), ps, ps, base);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void passesAndTypesSurviveRestart() throws Exception {
        List<String> first = run(new String[0], SETUP);
        assertFalse(first.stream().anyMatch(l -> l.startsWith("ERROR")), first.toString());
        List<String> passes = Files.readAllLines(base.resolve("passes.txt"));
        assertEquals(11, passes.size());
        assertTrue(passes.contains("SPEED;P02-1;T001;8.00;true"));
        assertTrue(passes.contains("SPEED;P02-2;T001;8.00;false"));
        assertTrue(passes.contains("SPECIAL;S01;T001;40.00;true"));
        assertEquals(List.of("A01;Tornado;20;GENERAL", "A04;Magia;5;SPECIAL"),
                Files.readAllLines(base.resolve("attractions.txt")));

        List<String> second = run(new String[0], String.join("\n", "SYSTEM DATE;2026-10-15",
                "ATTRACTION STATUS;A04", "ATTRACTION ENTER;A04;T001", "ATTRACTION ENTER;A01;T001;SPEED",
                "SYSTEM DATE;2026-10-01", "SPECIALPASS DELETE;S01", "SPEEDPASS DELETE;P02-1", "SPEEDPASS DELETE;P02-10",
                "EXIT") + "\n");
        assertTrue(second.contains("  Type: SPECIAL"), second.toString());
        assertTrue(second.get(second.indexOf("  Present: none") + 1).startsWith("ERROR: Special Pass S01"),
                "consumed Special Pass restored as consumed: " + second);
        assertTrue(second.stream().anyMatch(l -> l.endsWith("SpeedPass consumed: P02-2")), second.toString());
        assertTrue(second.stream().anyMatch(l -> l.startsWith("ERROR: Pass S01 has been consumed")));
        assertTrue(second.stream().anyMatch(l -> l.startsWith("ERROR: Pass P02-1 has been consumed")));
        assertTrue(second.contains("OK: SpeedPass refunded"));
    }

    @Test
    void legacyAttractionLineIsGeneral() throws Exception {
        Files.write(base.resolve("attractions.txt"), List.of("A01;Tornado;20"));
        List<String> out = run(new String[0], "ATTRACTION STATUS;A01\nEXIT\n");
        assertTrue(out.contains("  Type: GENERAL"), out.toString());
    }

    @Test
    void flyIsolatesF3Data() throws Exception {
        run(new String[0], "ATTRACTION CREATE;A09;Perm;3;GROUP\nEXIT\n");
        byte[] attractionsBefore = Files.readAllBytes(base.resolve("attractions.txt"));
        List<String> fly = run(new String[] {"--fly"}, SETUP.replace("EXIT\n",
                "TICKET CREATE;T003;ana@correo.com;2026-10-20\nSPEEDPASS CREATE;P09;T003;1\nTICKET DELETE;T003\nEXIT\n"));
        assertFalse(fly.stream().anyMatch(l -> l.startsWith("ERROR")), fly.toString());
        assertArrayEquals(attractionsBefore, Files.readAllBytes(base.resolve("attractions.txt")));
        assertFalse(Files.exists(base.resolve("passes.txt")));
        assertFalse(Files.exists(base.resolve("tickets.txt")));
        assertFalse(Files.exists(base.resolve("tmp")));
    }
}
