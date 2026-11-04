package com.parque;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.business.service.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {
    static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String SCENARIO = String.join("\n", ANA,
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "SYSTEM DATE;2026-10-01", "GROUP CREATE;G01;Familia", "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
            "ATTRACTION CREATE;A01;Tornado;20", "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999") + "\n";

    @TempDir
    Path base;

    record Run(int status, List<String> out, String err) {
    }

    private Run run(String[] args, InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), base);
        return new Run(status, out.toString(StandardCharsets.UTF_8).lines().toList(), err.toString(StandardCharsets.UTF_8));
    }

    private Run run(String[] args, String stdin) {
        return run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void directoryNamedByTimestampWithSuffixOnCollision() throws Exception {
        LocalDateTime t = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        Path first = DataDirectory.createTemporary(base, t);
        Path second = DataDirectory.createTemporary(base, t);
        assertEquals(base.resolve("tmp").resolve("20260826_190315"), first);
        assertEquals(base.resolve("tmp").resolve("20260826_190315_1"), second);
        Files.writeString(first.resolve("users.txt"), "x");
        DataDirectory.delete(first);
        assertFalse(Files.exists(first));
        assertTrue(Files.exists(base.resolve("tmp")), "tmp kept while another run directory exists");
        DataDirectory.delete(second);
        assertFalse(Files.exists(base.resolve("tmp")), "empty tmp removed");
    }

    @Test
    void interactiveFlyRunUsesTemporaryDirectoryAndCleansUp() {
        // Observe the temporary directory while the run is in progress.
        AtomicReference<List<Path>> seen = new AtomicReference<>();
        InputStream in = new ByteArrayInputStream((SCENARIO + "EXIT\n").getBytes(StandardCharsets.UTF_8)) {
            @Override
            public synchronized int read(byte[] b, int off, int len) {
                int n = super.read(b, off, len);
                if (n == -1 || available() == 0) {
                    try (var s = Files.walk(base)) {
                        seen.compareAndSet(null, s.filter(Files::isDirectory).toList());
                    } catch (Exception ignored) {
                        // only for observation
                    }
                }
                return n;
            }
        };
        Run r = run(new String[] {"--fly"}, in);
        assertEquals(0, r.status(), r.err());
        assertFalse(r.out().stream().anyMatch(l -> l.startsWith("ERROR")), r.out().toString());
        assertEquals("OK: Exiting", r.out().get(r.out().size() - 1));
        assertTrue(seen.get().stream().anyMatch(p -> p.getParent() != null && p.getParent().endsWith("tmp")
                && p.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?")), seen.get().toString());
        assertFalse(Files.exists(base.resolve("tmp")), "temporary directory deleted after EXIT");
        assertFalse(Files.exists(base.resolve("users.txt")), "permanent files not created");
    }

    @Test
    void fileFlyRunLeavesPermanentDataUntouched() throws Exception {
        assertEquals(0, run(new String[0], ANA + "\nATTRACTION CREATE;A01;Tornado;20\n").status());
        byte[] usersBefore = Files.readAllBytes(base.resolve("users.txt"));
        byte[] attractionsBefore = Files.readAllBytes(base.resolve("attractions.txt"));
        Path input = base.resolve("input_fase2.txt");
        Files.writeString(input, SCENARIO + "USER DELETE;pedro@correo.com;Otra2#\n");
        Run r = run(new String[] {"--fly", input.toString()}, "");
        assertEquals(0, r.status(), r.err());
        assertEquals("OK: User created", r.out().get(0), "independent starting state: Ana can be created again");
        assertArrayEquals(usersBefore, Files.readAllBytes(base.resolve("users.txt")));
        assertArrayEquals(attractionsBefore, Files.readAllBytes(base.resolve("attractions.txt")));
        for (String f : List.of("groups.txt", "tickets.txt", "grouptickets.txt")) {
            assertFalse(Files.exists(base.resolve(f)), f + " must not be written by a fly run");
        }
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void sameResultsAsNormalRun() throws Exception {
        Path input = base.resolve("seq.txt");
        Files.writeString(input, SCENARIO + "HELP\nEXIT\n");
        Run fly = run(new String[] {"--fly", input.toString()}, "");
        Run normal = run(new String[] {input.toString()}, "");
        assertEquals(normal.out(), fly.out());
    }

    @Test
    void missingFileInFlyModeCleansUp() {
        Run r = run(new String[] {"--fly", base.resolve("missing.txt").toString()}, "");
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"), r.err());
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void flyAfterFileArgumentAlsoRecognised() throws Exception {
        Path input = base.resolve("in.txt");
        Files.writeString(input, ANA + "\nEXIT\n");
        Run r = run(new String[] {input.toString(), "--fly"}, "");
        assertEquals(List.of("OK: User created", "OK: Exiting"), r.out());
        assertFalse(Files.exists(base.resolve("users.txt")));
    }

    @Test
    void normalRunStillPersistsAndCreatesNoTmp() {
        run(new String[0], ANA + "\n");
        assertTrue(Files.exists(base.resolve("users.txt")));
        assertFalse(Files.exists(base.resolve("tmp")));
        Run again = run(new String[0], ANA + "\n");
        assertTrue(again.out().get(0).startsWith("ERROR: "), "user persisted by the normal run");
    }
}
