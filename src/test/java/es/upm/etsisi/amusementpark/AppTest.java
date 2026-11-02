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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String SEQUENCE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Dup;Dup;11111111H;ana@correo.com;Clave1!;1;1234567890123456",
            "NOT A COMMAND",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER UPDATE;ana.nueva@correo.com;Clave1!;DNI;87654321X",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Late;User;00000000T;late@correo.com;Clave1!;1;1234567890123456",
            "");

    private static final String PHASE2 = String.join("\n",
            "SYSTEM DATE;2099-04-15",
            "SYSTEM PRICE;45.50",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2099-04-15",
            "GROUPTICKET CREATE;TG01;G01;2099-04-15",
            "GROUP ADD;G01;ana@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;ana@correo.com",
            "ATTRACTION STATUS;A01",
            "USER DELETE;pedro@correo.com;Otra2#",
            "EXIT",
            "");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path base) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), base);
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
        String fromFile = output();

        assertEquals(interactive, fromFile);
        assertEquals(Files.readAllLines(a.resolve("users.txt")), Files.readAllLines(b.resolve("users.txt")));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(b.resolve("users.txt")));
        List<String> lines = fromFile.lines().toList();
        assertEquals("USER CREATED", lines.get(0));
        assertEquals("USER CREATED", lines.get(1));
        assertTrue(lines.get(2).startsWith("ERROR:"));
        assertTrue(lines.get(3).startsWith("ERROR:"));
        assertEquals("USER UPDATED", lines.get(4));
        assertEquals("USER UPDATED", lines.get(5));
        assertTrue(lines.get(6).startsWith("ERROR:"));
        assertEquals("USER DELETED", lines.get(7));
        assertTrue(fromFile.contains("USER DELETE;<email>;<password>"));
        assertEquals("BYE", lines.get(lines.size() - 1));
    }

    @Test
    void usersSurviveRestart() throws Exception {
        Path base = base("r");
        run(new String[0], "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n", base);
        out.reset();
        run(new String[0], "USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;1;1234567890123456\n"
                + "USER DELETE;ana@correo.com;Clave1!\n", base);
        List<String> lines = output().lines().toList();
        assertTrue(lines.get(0).startsWith("ERROR:"), "email still reserved after restart");
        assertEquals("USER DELETED", lines.get(1));
        assertTrue(Files.readAllLines(base.resolve("users.txt")).isEmpty());
    }

    @Test
    void phaseTwoFileModeAndFlyModeMatch() throws Exception {
        Path commands = dir.resolve("input_fase2.txt");
        Files.writeString(commands, PHASE2);
        Path normal = base("normal");
        assertEquals(0, run(new String[] {commands.toString()}, "", normal));
        String fileOutput = output();
        out.reset();
        Path flyBase = base("fly");
        assertEquals(0, run(new String[] {App.FLY, commands.toString()}, "", flyBase));
        assertEquals(fileOutput, output());
        assertFalse(Files.exists(flyBase.resolve(App.TMP_DIR)));
        try (var files = Files.list(flyBase)) {
            assertEquals(0, files.count());
        }
        List<String> lines = fileOutput.lines().toList();
        assertTrue(lines.contains("TICKET CREATED; PRICE: 45.50 EUR"));
        assertTrue(lines.contains("GROUP TICKET CREATED; MEMBERS: 2; TOTAL PRICE: 91.00 EUR"));
        assertTrue(lines.contains("ENTERED A01: ana@correo.com, pedro@correo.com"));
        assertTrue(lines.contains("INSIDE: 1"));
        assertTrue(lines.contains("AVAILABLE: 19"));
        assertEquals(4, lines.stream().filter(l -> l.startsWith("ERROR:")).count() + 2);
        assertTrue(Files.readString(normal.resolve("tickets.txt")).contains("GROUP;TG01;2099-04-15;G01"));
        assertEquals(2, Files.readAllLines(normal.resolve("users.txt")).size());
    }

    @Test
    void interactiveFlyLeavesNoData() throws Exception {
        Path base = base("ifly");
        Files.writeString(base.resolve("users.txt"),
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n");
        String before = Files.readString(base.resolve("users.txt"));
        assertEquals(0, run(new String[] {App.FLY},
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                        + "USER DELETE;pedro@correo.com;Otra2#\nEXIT\n", base));
        assertEquals(List.of("USER CREATED", "ERROR: user not found", "BYE"), output().lines().toList());
        assertFalse(Files.exists(base.resolve(App.TMP_DIR)));
        assertEquals(before, Files.readString(base.resolve("users.txt")));
    }

    @Test
    void flyWithMissingFileStillCleansUp() throws Exception {
        Path base = base("mfly");
        assertEquals(1, run(new String[] {App.FLY, dir.resolve("nope.txt").toString()}, "", base));
        assertFalse(Files.exists(base.resolve(App.TMP_DIR)));
    }

    @Test
    void flyFailsInControlledWayWhenTmpCannotBeCreated() throws Exception {
        Path base = base("bfly");
        Files.writeString(base.resolve(App.TMP_DIR), "not a directory");
        assertEquals(1, run(new String[] {App.FLY}, "EXIT\n", base));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("temporary directory"));
    }

    @Test
    void missingCommandFileIsControlled() throws Exception {
        Path base = base("m");
        assertEquals(1, run(new String[] {dir.resolve("nope.txt").toString()}, "", base));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("not found or not readable"));
        assertFalse(Files.exists(base.resolve("users.txt")));
    }

    @Test
    void directoryAsCommandFileIsControlled() throws Exception {
        assertEquals(1, run(new String[] {dir.toString()}, "", base("d")));
    }

    @Test
    void unreadableUsersFileIsControlled() throws Exception {
        Path base = base("u");
        Files.createDirectories(base.resolve("users.txt"));
        assertEquals(1, run(new String[0], "EXIT\n", base));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("could not load park data"));
    }

    @Test
    void startDateIsToday() throws Exception {
        assertEquals(0, run(new String[0], "TICKET CREATE;T;x@y.com;" + LocalDate.now().minusDays(1) + "\nEXIT\n", base("t")));
        assertTrue(output().contains("not found"));
    }
}
