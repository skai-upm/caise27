package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir.resolve("users.txt"));
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
        assertEquals(List.of(
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
    public void usersSurviveRestart() throws Exception {
        run(new String[0], CREATE_ANA + "\nEXIT\n");
        buffer.reset();
        run(new String[0], "USER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertTrue(output().contains("OK: user deleted"), output());
        assertTrue(storedUsers().isEmpty());
    }
}
