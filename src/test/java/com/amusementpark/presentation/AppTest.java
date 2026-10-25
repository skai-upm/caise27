package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.TestSupport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private int run(String[] args, String stdin) {
        return App.run(args, new BufferedReader(new StringReader(stdin)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private List<String> storedUsers() throws Exception {
        return Files.readAllLines(dir.resolve("users.txt"));
    }

    @Test
    public void fileModeRunsCommandsInOrderAndStopsAtExit() throws Exception {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, List.of(
                CREATE_ANA,
                "",
                CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "EXIT",
                "USER DELETE;pedro@correo.com;Otra2#"));
        assertEquals(0, run(new String[] {commands.toString()}, ""));
        TestSupport.assertStoredUsers(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                storedUsers());
        assertFalse(output().contains("ERROR"), output());
    }

    @Test
    public void interactiveModeReadsStandardInputUntilExit() throws Exception {
        assertEquals(0, run(new String[0], CREATE_ANA + "\nHELP\nEXIT\n" + CREATE_PEDRO + "\n"));
        assertEquals(1, storedUsers().size());
        assertTrue(output().contains("USER DELETE;Correo;Contraseña"));
    }

    @Test
    public void interactiveModeEndsWithInput() throws Exception {
        assertEquals(0, run(new String[0], CREATE_ANA + "\n"));
        assertEquals(1, storedUsers().size());
    }

    @Test
    public void fileAndInteractiveProduceSameResult() throws Exception {
        Path commands = dir.resolve("cmds.txt");
        Files.write(commands, List.of("FOO", "HELP"));
        run(new String[] {commands.toString()}, "");
        String fromFile = output();
        buffer.reset();
        run(new String[0], "FOO\nHELP\n");
        assertTrue(output().endsWith(fromFile), output());
    }

    @Test
    public void missingFileIsReportedWithoutException() {
        assertEquals(1, run(new String[] {dir.resolve("missing.txt").toString()}, ""));
        assertTrue(output().contains("ERROR"));
        assertTrue(output().contains("does not exist"));
    }

    @Test
    public void unreadableDirectoryIsReported() {
        assertEquals(1, run(new String[] {dir.toString()}, ""));
        assertTrue(output().contains("ERROR"));
    }

    @Test
    public void flyModeKeepsNormalDataUntouchedAndCleansUp() throws Exception {
        run(new String[0], CREATE_ANA + "\nEXIT\n");
        List<String> before = storedUsers();
        buffer.reset();
        assertEquals(0, run(new String[] {"--fly"}, CREATE_PEDRO + "\nUSER DELETE;ana@correo.com;Clave1!\nEXIT\n"));
        assertTrue(output().contains("Fly mode"), output());
        assertTrue(output().contains("OK: user created"), output());
        assertTrue(output().contains("ERROR: unknown email"), output());
        assertEquals(before, storedUsers());
        assertTempDirEmpty();
    }

    @Test
    public void flyModeWithCommandFileAndEndOfInput() throws Exception {
        Path commands = dir.resolve("input_fase2.txt");
        Files.write(commands, List.of(CREATE_ANA, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com"));
        assertEquals(0, run(new String[] {"--fly", commands.toString()}, ""));
        assertFalse(output().contains("ERROR"), output());
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertTempDirEmpty();
    }

    @Test
    public void flyModeCoversE3Data() throws Exception {
        Path commands = dir.resolve("input_fase3.txt");
        Files.write(commands, List.of(CREATE_ANA, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001", "ATTRACTION CREATE;A01;Casa;5;SPECIAL",
                "INVOICE CREATE;USER;ana@correo.com"));
        assertEquals(0, run(new String[] {"--fly", commands.toString()}, ""));
        assertFalse(output().contains("ERROR"), output());
        assertTrue(output().contains("TOTAL: 160.00"), output());
        for (String name : new String[] {"users.txt", "tickets.txt", "passes.txt", "attractions.txt"}) {
            assertFalse(Files.exists(dir.resolve(name)), name);
        }
        assertTempDirEmpty();
    }

    @Test
    public void flyModeCleansUpAfterFileError() throws Exception {
        assertEquals(1, run(new String[] {"--fly", dir.resolve("missing.txt").toString()}, ""));
        assertTempDirEmpty();
    }

    @Test
    public void flyDirectoriesAreUnique() throws Exception {
        Path first = App.createFlyDirectory(dir);
        Path second = App.createFlyDirectory(dir);
        assertTrue(Files.isDirectory(first) && Files.isDirectory(second));
        assertFalse(first.equals(second));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
    }

    private void assertTempDirEmpty() throws Exception {
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    public void usersSurviveRestart() throws Exception {
        run(new String[0], CREATE_ANA + "\nEXIT\n");
        buffer.reset();
        run(new String[0], "USER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertTrue(output().contains("OK: user deleted"), output());
        assertTrue(storedUsers().isEmpty());
    }
}
