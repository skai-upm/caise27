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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> BASIC = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private record Run(int status, String output) {
    }

    private Run run(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        int status = App.run(args, in, new PrintStream(bytes, true, StandardCharsets.UTF_8), usersFile);
        return new Run(status, bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void fileModeProcessesCommandsInOrder() throws Exception {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, BASIC, StandardCharsets.UTF_8);
        Path users = dir.resolve("users.txt");

        Run r = run(new String[] {commands.toString()}, "", users);

        assertEquals(0, r.status());
        List<String> stored = Files.readAllLines(users, StandardCharsets.UTF_8);
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).contains(";699999999;"));
        for (String syntax : CommandProcessor.HELP_LINES) {
            assertTrue(r.output().contains(syntax), syntax);
        }
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws Exception {
        Path commands = dir.resolve("cmds.txt");
        Files.write(commands, List.of(BASIC.get(0), "", "EXIT", BASIC.get(1)), StandardCharsets.UTF_8);
        Path users = dir.resolve("users.txt");

        run(new String[] {commands.toString()}, "", users);

        assertEquals(1, Files.readAllLines(users, StandardCharsets.UTF_8).size());
    }

    @Test
    void interactiveAndFileModesAreEquivalent() throws Exception {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, BASIC, StandardCharsets.UTF_8);
        Path usersFromFile = dir.resolve("users-file.txt");
        Path usersFromStdin = dir.resolve("users-stdin.txt");

        Run fromFile = run(new String[] {commands.toString()}, "", usersFromFile);
        Run fromStdin = run(new String[0], String.join("\n", BASIC) + "\nUSER DELETE;ana@correo.com;Clave1!\n", usersFromStdin);

        assertEquals(fromFile.output(), fromStdin.output());
        assertEquals(Files.readAllLines(usersFromFile), Files.readAllLines(usersFromStdin));
    }

    @Test
    void groupsSurviveRestart() throws Exception {
        Path users = dir.resolve("users.txt");
        run(new String[0], String.join("\n", BASIC.get(0), BASIC.get(1),
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com"), users);
        Run second = run(new String[0], "GROUP ADD;G01;ana@correo.com\nGROUP REMOVE;G01;pedro@correo.com\n", users);
        assertTrue(second.output().startsWith("ERROR"));
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void ticketsSurviveRestartAndPriceResets() throws Exception {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], String.join("\n", BASIC.get(0), "SYSTEM DATE;2026-10-01", "SYSTEM PRICE;60.00",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15"), users);
        assertTrue(first.output().contains("OK TICKET CREATED -> 60.00"));
        Run second = run(new String[0], String.join("\n", "SYSTEM DATE;2026-10-01",
                "USER DELETE;ana@correo.com;Clave1!", "TICKET CREATE;T002;ana@correo.com;2026-10-20",
                "SYSTEM DATE;2026-10-16", "USER DELETE;ana@correo.com;Clave1!"), users);
        List<String> lines = second.output().lines().toList();
        assertTrue(lines.get(1).startsWith("ERROR"), lines.get(1));
        assertEquals("OK TICKET CREATED -> 40.00", lines.get(2));
        assertTrue(lines.get(4).startsWith("ERROR"), "T002 (2026-10-20) still blocks deletion: " + lines.get(4));
        assertEquals(List.of("T001;2026-10-15;12345678Z;60.00", "T002;2026-10-20;12345678Z;40.00"),
                Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void groupTicketsSurviveRestartAndKeepTheGroupLocked() throws Exception {
        Path users = dir.resolve("users.txt");
        run(new String[0], String.join("\n", BASIC.get(0), BASIC.get(1), "SYSTEM DATE;2026-10-01",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15"), users);
        Run second = run(new String[0], String.join("\n", "SYSTEM DATE;2026-10-01", "GROUP REMOVE;G01;ana@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "SYSTEM DATE;2026-10-16", "GROUP REMOVE;G01;ana@correo.com"), users);
        List<String> lines = second.output().lines().toList();
        assertTrue(lines.get(1).startsWith("ERROR"), lines.get(1));
        assertTrue(lines.get(2).startsWith("ERROR"), lines.get(2));
        assertTrue(lines.get(4).startsWith("OK"), lines.get(4));
        assertEquals(List.of("TG01;2026-10-15;G01;40.00;80.00;12345678Z;87654321X"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() throws Exception {
        Path users = dir.resolve("users.txt");
        Run r = run(new String[0], BASIC.get(0) + "\n", users);
        assertEquals(0, r.status());
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void missingFileIsReportedInAControlledWay() {
        Path missing = dir.resolve("does-not-exist.txt");
        Run r = run(new String[] {missing.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.output().startsWith("ERROR"));
        assertTrue(r.output().contains("does-not-exist.txt"));
        assertFalse(r.output().contains("\tat "));
    }

    @Test
    void directoryPathIsReportedInAControlledWay() {
        Run r = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.output().startsWith("ERROR"));
    }

    @Test
    void unreadableContentIsReportedInAControlledWay() throws Exception {
        Path bad = dir.resolve("bad.txt");
        Files.write(bad, new byte[] {(byte) 0xC3, (byte) 0x28, '\n'});
        Run r = run(new String[] {bad.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.output().startsWith("ERROR"));
    }
}
