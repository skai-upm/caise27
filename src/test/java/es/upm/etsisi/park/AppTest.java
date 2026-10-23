package es.upm.etsisi.park;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.persistence.text.TextRepositories;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private Path usersFile;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        usersFile = dir.resolve("users.txt");
        System.setProperty(TextRepositories.DATA_DIR_PROPERTY, dir.toString());
        System.setProperty(App.FLY_DIR_PROPERTY, dir.resolve("tmp").toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(TextRepositories.DATA_DIR_PROPERTY);
        System.clearProperty(App.FLY_DIR_PROPERTY);
    }

    private int run(InputStream in, String... args) {
        return App.run(args, in,
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Test
    void fileModeProcessesInOrderAndStopsAtExit() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(CREATE_ANA, "", "   ", CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "EXIT",
                "USER DELETE;pedro@correo.com;Otra2#"));

        assertEquals(0, run(noInput(), input.toString()));

        String text = out.toString(StandardCharsets.UTF_8);
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "BYE"), text.lines().toList());
        List<String> stored = Files.readAllLines(usersFile);
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertTrue(stored.get(0).endsWith(";699999999;1234567890123456"));
        assertTrue(stored.get(1).startsWith("Pedro;Lopez Martin;87654321X;pedro@correo.com;$2a$"));
    }

    @Test
    void interactiveModeUsesSameProcessing() {
        String commands = CREATE_ANA + "\nHELP\nEXIT\n" + CREATE_PEDRO + "\n";
        assertEquals(0, run(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8))));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("USER CREATED"));
        assertTrue(text.contains("USER DELETE;<email>;<password>"));
        assertTrue(text.trim().endsWith("BYE"));
        assertFalse(text.contains("pedro"));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        assertEquals(0, run(new ByteArrayInputStream("HELP\n".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void usersPersistBetweenRuns() throws Exception {
        Path first = dir.resolve("first.txt");
        Files.write(first, List.of(CREATE_ANA, "EXIT"));
        run(noInput(), first.toString());

        Path second = dir.resolve("second.txt");
        Files.write(second, List.of("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;ana.nueva@correo.com;Clave1!", "EXIT"));
        out.reset();
        run(noInput(), second.toString());
        assertEquals(List.of("USER UPDATED", "USER DELETED", "BYE"),
                out.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void missingCommandFileIsControlled() {
        assertEquals(1, run(noInput(), dir.resolve("missing.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void directoryAsCommandFileIsControlled() {
        assertEquals(1, run(noInput(), dir.toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR"));
    }

    @Test
    void flyModeWithFileLeavesPermanentDataUntouched() throws Exception {
        Files.write(usersFile, List.of(
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        List<String> before = Files.readAllLines(usersFile);
        Path input = dir.resolve("input_fase2.txt");
        Files.write(input, List.of(CREATE_ANA, CREATE_PEDRO, "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com", "ATTRACTION CREATE;A01;Dragon Rojo;20", "EXIT"));

        assertEquals(0, run(noInput(), "--fly", input.toString()));

        assertEquals(List.of("USER CREATED", "USER CREATED", "GROUP CREATED", "USER ADDED TO GROUP",
                "ATTRACTION CREATED", "BYE"), out.toString(StandardCharsets.UTF_8).lines().toList());
        assertEquals(before, Files.readAllLines(usersFile));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyModeInteractiveStartsEmptyAndCleansUp() throws Exception {
        Files.write(usersFile, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        String commands = "USER DELETE;ana@correo.com;Clave1!\n" + CREATE_ANA + "\n";
        assertEquals(0, run(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)), "--fly"));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ERROR: User with email ana@correo.com not found"));
        assertTrue(text.contains("USER CREATED"));
        assertEquals(1, Files.readAllLines(usersFile).size());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyModeKeepsExistingBaseDirectory() throws Exception {
        Files.createDirectories(dir.resolve("tmp"));
        assertEquals(0, run(new ByteArrayInputStream("EXIT\n".getBytes(StandardCharsets.UTF_8)), "--fly"));
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void flyModeMissingFileIsControlled() {
        assertEquals(1, run(noInput(), "--fly", dir.resolve("missing.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyDirectoriesAreUnique() throws Exception {
        Path base = dir.resolve("base");
        Path first = App.createFlyDirectory(base);
        Path second = App.createFlyDirectory(base);
        assertTrue(Files.isDirectory(first));
        assertTrue(Files.isDirectory(second));
        assertFalse(first.equals(second));
    }

    @Test
    void parkStateSurvivesRestartThroughJarEntryPoint() throws Exception {
        Path first = dir.resolve("first.txt");
        Files.write(first, List.of("SYSTEM DATE;2027-04-15", CREATE_ANA, CREATE_PEDRO, "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20", "ATTRACTION CREATE;A01;Dragon Rojo;20", "EXIT"));
        assertEquals(0, run(noInput(), first.toString()));

        Path second = dir.resolve("second.txt");
        Files.write(second, List.of("SYSTEM DATE;2027-04-15", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET DELETE;TG01", "ATTRACTION STATUS;A01", "EXIT"));
        out.reset();
        assertEquals(0, run(noInput(), second.toString()));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ERROR: User ana@correo.com already belongs to group G01"));
        assertTrue(text.contains("GROUP TICKET DELETED"));
        assertTrue(text.contains("Capacity: 20"));
    }

    @Test
    void flyModeCoversPassesAndCleansUp() throws Exception {
        Path input = dir.resolve("input_fase3.txt");
        Files.write(input, List.of("SYSTEM DATE;2027-04-15", CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "SPEEDPASS CREATE;SP01;T001;1", "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION ENTER;A01;T001;SPEED", "EXIT"));
        assertEquals(0, run(noInput(), "--fly", input.toString()));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("SPEEDPASS CONSUMED: SP01:1"));
        assertFalse(Files.exists(dir.resolve("passes.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
