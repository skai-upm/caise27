package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    static final String[] SESSION = {TestData.CREATE_ANA, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
        "SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20",
        "ATTRACTION ENTER;A01;T001", "ATTRACTION STATUS;A01", "USER CREATE;Ana;X;12345678Z;ana@correo.com;Clave1!;6;1234567890123456"};

    @TempDir
    Path work;

    private List<String> execute(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.execute(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), work);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private static String lines(String... l) {
        return String.join("\n", l) + "\n";
    }

    @Test
    void interactiveFlyRunLeavesPermanentDataUntouchedAndCleansUp() throws IOException {
        Files.write(work.resolve("users.txt"), List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        byte[] before = Files.readAllBytes(work.resolve("users.txt"));
        List<String> out = execute(lines(SESSION) + "USER DELETE;pedro@correo.com;Otra2#\nEXIT\n", "--fly");
        assertTrue(out.get(0).startsWith("OK:"));
        assertTrue(out.get(out.size() - 2).startsWith("ERROR:")); // Pedro does not exist in the isolated data
        assertEquals("OK: Bye", out.get(out.size() - 1));
        assertTrue(java.util.Arrays.equals(before, Files.readAllBytes(work.resolve("users.txt"))));
        for (String f : new String[] {"groups.txt", "tickets.txt", "grouptickets.txt", "attractions.txt"}) {
            assertFalse(Files.exists(work.resolve(f)), f);
        }
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void fileFlyRunUsesTemporaryData() throws IOException {
        Path input = work.resolve("input_fase2.txt");
        Files.write(input, List.of(SESSION));
        List<String> fly = execute("", "--fly", "input_fase2.txt");
        assertFalse(Files.exists(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("tmp")));
        List<String> normal = execute("", "input_fase2.txt");
        assertEquals(normal, fly);
        assertTrue(Files.exists(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void flyOptionMayFollowTheFile() throws IOException {
        Files.write(work.resolve("cmds.txt"), List.of(TestData.CREATE_ANA, "EXIT"));
        List<String> out = execute("", "cmds.txt", "--fly");
        assertEquals(List.of("OK: User ana@correo.com created", "OK: Bye"), out);
        assertFalse(Files.exists(work.resolve("users.txt")));
    }

    @Test
    void endOfInputAndMissingFileAlsoCleanUp() {
        execute(lines(TestData.CREATE_ANA), "--fly");
        assertFalse(Files.exists(work.resolve("tmp")));
        List<String> out = execute("", "--fly", "missing.txt");
        assertTrue(out.get(0).startsWith("ERROR: Cannot read command file"));
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void directoryExistsDuringTheRunAndIsUnique() throws IOException {
        Path tmpRoot = work.resolve("tmp");
        LocalDateTime start = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        Path first = App.createTemporaryDataDir(tmpRoot, start);
        Path second = App.createTemporaryDataDir(tmpRoot, start);
        assertEquals("20260826_190315", first.getFileName().toString());
        assertEquals("20260826_190315_2", second.getFileName().toString());
        assertNotEquals(first, second);
        Files.writeString(first.resolve("users.txt"), "x");
        App.deleteTemporaryDataDir(first, tmpRoot);
        assertFalse(Files.exists(first));
        assertTrue(Files.exists(tmpRoot));          // another run's directory remains
        App.deleteTemporaryDataDir(second, tmpRoot);
        assertFalse(Files.exists(tmpRoot));         // removed once empty
    }

    @Test
    void temporaryDirectoryHoldsTheDataWhileRunning() {
        AtomicReference<Boolean> seen = new AtomicReference<>(false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream spy = new PrintStream(out, true, StandardCharsets.UTF_8) {
            @Override
            public void println(String x) {
                super.println(x);
                try (var dirs = Files.list(work.resolve("tmp"))) {
                    seen.set(seen.get() || dirs.anyMatch(d -> Files.exists(d.resolve("users.txt"))));
                } catch (IOException e) {
                    // not created
                }
            }
        };
        App.execute(new String[] {"--fly"},
                new ByteArrayInputStream((TestData.CREATE_ANA + "\nEXIT\n").getBytes(StandardCharsets.UTF_8)), spy, work);
        assertTrue(seen.get());
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void normalModeIsUnchanged() throws IOException {
        List<String> out = execute(lines(TestData.CREATE_ANA, "EXIT"));
        assertEquals("OK: User ana@correo.com created", out.get(0));
        assertTrue(Files.exists(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void delivery3DataIsIsolatedToo() throws IOException {
        Files.write(work.resolve("passes.txt"), List.of("X1;SPEED;T900;8.00;false"));
        Files.write(work.resolve("attractions.txt"), List.of("A09;Antigua;5"));
        List<String> out = execute(lines(TestData.CREATE_ANA, "SYSTEM DATE;2026-10-15",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "SPECIALPASS CREATE;SP1;T001",
                "ATTRACTION CREATE;A04;Vip;5;SPECIAL", "ATTRACTION ENTER;A04;T001", "EXIT"), "--fly");
        assertTrue(out.get(5).endsWith("consumed Special Pass SP1"));
        assertEquals(List.of("X1;SPEED;T900;8.00;false"), Files.readAllLines(work.resolve("passes.txt")));
        assertEquals(List.of("A09;Antigua;5"), Files.readAllLines(work.resolve("attractions.txt")));
        assertFalse(Files.exists(work.resolve("tmp")));
    }
}
