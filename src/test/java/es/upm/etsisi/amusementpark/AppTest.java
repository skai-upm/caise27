package es.upm.etsisi.amusementpark;

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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String SEQUENCE = String.join("\n",
            TestSupport.ANA, "", TestSupport.PEDRO,
            "USER CREATE;Dup;Dup;11111111H;ana@correo.com;Clave1!;1;1234567890123456",
            "NOT A COMMAND",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "EXIT",
            "USER CREATE;Late;User;00000000T;late@correo.com;Clave1!;1;1234567890123456", "");

    private static final String PHASE3 = String.join("\n",
            "SYSTEM DATE;2099-04-15", TestSupport.ANA, TestSupport.PEDRO,
            "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2099-04-15", "GROUPTICKET CREATE;TG01;G01;2099-04-15",
            "SPEEDPASS CREATE;SP01;T001;10", "SPECIALPASS CREATE;PE01;T001",
            "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATE;A04;Terror;15;SPECIAL",
            "ATTRACTION ENTER;A01;T001;SPEED", "ATTRACTION ENTER;A04;T001", "ATTRACTION ENTER;A04;TG01",
            "INVOICE CREATE;USER;ana@correo.com", "INVOICE CREATE;GROUP;G01", "HELP EXTENDED", "EXIT", "");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path base) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), base, TestSupport.HASHER);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private Path base(String name) throws Exception {
        return Files.createDirectories(dir.resolve(name));
    }

    @Test
    void interactiveAndFileModesBehaveIdentically() throws Exception {
        Path a = base("a");
        assertEquals(0, run(new String[0], SEQUENCE, a));
        String interactive = output();
        out.reset();
        Path commands = dir.resolve("input.txt");
        Files.writeString(commands, SEQUENCE);
        Path b = base("b");
        assertEquals(0, run(new String[] {commands.toString()}, "", b));
        assertEquals(interactive, output());
        List<String> lines = output().lines().toList();
        assertEquals(List.of("USER CREATED", "USER CREATED"), lines.subList(0, 2));
        assertTrue(lines.get(2).startsWith("ERROR:") && lines.get(3).startsWith("ERROR:"));
        assertEquals("BYE", lines.get(lines.size() - 1));
        List<String> stored = Files.readAllLines(b.resolve("users.txt"));
        assertEquals(1, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;$2a$"));
        assertFalse(stored.get(0).contains("Clave1!"));
    }

    @Test
    void phaseThreeFileAndFlyModesMatch() throws Exception {
        Path commands = dir.resolve("input_fase3.txt");
        Files.writeString(commands, PHASE3);
        Path normal = base("normal");
        assertEquals(0, run(new String[] {commands.toString()}, "", normal));
        String fileOutput = output();
        out.reset();
        Path fly = base("fly");
        assertEquals(0, run(new String[] {App.FLY, commands.toString()}, "", fly));
        assertEquals(fileOutput, output());
        try (Stream<Path> left = Files.list(fly)) {
            assertEquals(0, left.count());
        }
        assertTrue(fileOutput.contains("SPEEDPASS CREATED: SP01:1 .. SP01:10; TOTAL PRICE: 80.00 EUR"));
        assertTrue(fileOutput.contains("ENTERED A01: ana@correo.com; SPEEDPASS CONSUMED: SP01:1"));
        assertTrue(fileOutput.contains("ENTERED A04: ana@correo.com; SPECIAL PASS CONSUMED: PE01"));
        assertTrue(fileOutput.contains("TOTAL: 160.00 EUR"));
        assertTrue(fileOutput.contains("TOTAL: 80.00 EUR"));
        assertTrue(fileOutput.contains("Registers a user"));
        assertTrue(Files.readString(normal.resolve("passes.txt")).contains("SPEED;SP01:1;T001;8.00;true"));
    }

    @Test
    void interactiveFlyLeavesNoData() throws Exception {
        Path base = base("ifly");
        assertEquals(0, run(new String[] {App.FLY}, TestSupport.ANA + "\nEXIT\n", base));
        assertEquals(List.of("USER CREATED", "BYE"), output().lines().toList());
        assertFalse(Files.exists(base.resolve(App.TMP_DIR)));
        assertFalse(Files.exists(base.resolve("users.txt")));
    }

    @Test
    void controlledFailures() throws Exception {
        Path m = base("m");
        assertEquals(1, run(new String[] {dir.resolve("nope.txt").toString()}, "", m));
        assertEquals(1, run(new String[] {App.FLY, dir.resolve("nope.txt").toString()}, "", m));
        assertFalse(Files.exists(m.resolve(App.TMP_DIR)));
        assertEquals(1, run(new String[] {dir.toString()}, "", base("d")));
        Path u = base("u");
        Files.createDirectories(u.resolve("users.txt"));
        assertEquals(1, run(new String[0], "EXIT\n", u));
        Path f = base("f");
        Files.writeString(f.resolve(App.TMP_DIR), "not a directory");
        assertEquals(1, run(new String[] {App.FLY}, "EXIT\n", f));
        String errors = err.toString(StandardCharsets.UTF_8);
        assertTrue(errors.contains("not found or not readable"));
        assertTrue(errors.contains("could not load park data"));
        assertTrue(errors.contains("temporary directory"));
    }
}
