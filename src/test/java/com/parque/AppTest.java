package com.parque;

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
 * Runs the application end to end with in-memory standard streams, in both input modes.
 */
class AppTest {

    static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    /** Result of one application run. */
    record Run(int status, List<String> out, String err) {
    }

    private Run run(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8),
                usersFile.getParent());
        return new Run(status, out.toString(StandardCharsets.UTF_8).lines().toList(),
                err.toString(StandardCharsets.UTF_8));
    }

    private Path commandFile(String name, String content) throws Exception {
        Path f = dir.resolve(name);
        Files.writeString(f, content, StandardCharsets.UTF_8);
        return f;
    }

    @Test
    void interactiveSessionStopsAtExit() throws Exception {
        Path users = dir.resolve("users.txt");
        Run r = run(new String[0], String.join("\n", ANA, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "HELP", "EXIT", PEDRO) + "\n", users);
        assertEquals(0, r.status());
        assertEquals("OK: User created", r.out().get(0));
        assertEquals("OK: User updated", r.out().get(1));
        assertEquals("OK: Available commands:", r.out().get(2));
        assertEquals("OK: Exiting", r.out().get(r.out().size() - 1));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(users));
    }

    @Test
    void interactiveEndsAtEndOfInput() {
        Run r = run(new String[0], ANA + "\n", dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertEquals(List.of("OK: User created"), r.out());
    }

    @Test
    void fileExecutedInOrderAndStopsAtExit() throws Exception {
        Path users = dir.resolve("users.txt");
        Path file = commandFile("input_basico.txt", String.join("\n", ANA, PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "EXIT",
                "USER DELETE;pedro@correo.com;Otra2#") + "\n");
        Run r = run(new String[] {file.toString()}, "", users);
        assertEquals(0, r.status());
        assertEquals(List.of("OK: User created", "OK: User created", "OK: User updated", "OK: Exiting"), r.out());
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(users));
    }

    @Test
    void commandAfterExitNotExecuted() throws Exception {
        Path users = dir.resolve("users.txt");
        Path file = commandFile("cmds.txt", ANA + "\nEXIT\n" + PEDRO + "\n");
        Run r = run(new String[] {file.toString()}, "", users);
        assertEquals(List.of("OK: User created", "OK: Exiting"), r.out());
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void blankLinesIgnored() throws Exception {
        Path file = commandFile("blank.txt", ANA + "\n\n   \nHELP\n\nEXIT\n");
        Run r = run(new String[] {file.toString()}, "", dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertFalse(r.out().stream().anyMatch(l -> l.startsWith("ERROR")), r.out().toString());
        assertEquals("OK: User created", r.out().get(0));
        assertEquals("OK: Exiting", r.out().get(r.out().size() - 1));
    }

    @Test
    void sameSequenceSameResultInBothModes() throws Exception {
        String sequence = String.join("\n", ANA, PEDRO, "USER CREATE;X;Y;12345678A;x@correo.com;Clave1!;1;1",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com", "USER DELETE;pedro@correo.com;Mala1!",
                "HELP", "USER DELETE;pedro@correo.com;Otra2#", "EXIT") + "\n";
        Path usersA = dir.resolve("a/users.txt");
        Path usersB = dir.resolve("b/users.txt");
        Run interactive = run(new String[0], sequence, usersA);
        Run fromFile = run(new String[] {commandFile("seq.txt", sequence).toString()}, "", usersB);
        assertEquals(interactive.out(), fromFile.out());
        assertEquals(Files.readAllLines(usersA), Files.readAllLines(usersB));
    }

    @Test
    void missingFileHandled() {
        Path users = dir.resolve("users.txt");
        Run r = run(new String[] {dir.resolve("nope.txt").toString()}, ANA + "\n", users);
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"), r.err());
        assertTrue(r.err().contains("nope.txt"));
        assertFalse(r.err().contains("\tat "), "no stack trace");
        assertEquals(List.of(), r.out());
        assertFalse(Files.exists(users));
    }

    @Test
    void directoryPathHandled() {
        Run r = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"), r.err());
        assertFalse(r.err().contains("\tat "));
    }

    @Test
    void systemDateCommandFromFile() throws Exception {
        Path file = commandFile("dates.txt", "SYSTEM DATE;2026-09-15\nSYSTEM DATE;2026-02-30\nSYSTEM DATE;2025-01-01\nEXIT\n");
        Run r = run(new String[] {file.toString()}, "", dir.resolve("users.txt"));
        assertEquals(0, r.status());
        assertEquals("OK: System date set to 2026-09-15", r.out().get(0));
        assertTrue(r.out().get(1).startsWith("ERROR: Invalid date"));
        assertEquals("OK: System date set to 2025-01-01", r.out().get(2));
    }

    @Test
    void groupsRestoredAfterRestart() throws Exception {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], String.join("\n", ANA, PEDRO, "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com", "EXIT") + "\n", users);
        assertFalse(first.out().stream().anyMatch(l -> l.startsWith("ERROR")), first.out().toString());
        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X"),
                Files.readAllLines(dir.resolve("groups.txt")));
        Run second = run(new String[0], String.join("\n", "GROUP ADD;G01;ana.nueva@correo.com",
                "GROUP REMOVE;G01;ana.nueva@correo.com", "USER DELETE;pedro@correo.com;Otra2#", "EXIT") + "\n", users);
        assertTrue(second.out().get(0).startsWith("ERROR: "), "Ana is still a member after restart");
        assertEquals("OK: User removed from group", second.out().get(1));
        assertEquals("OK: User deleted", second.out().get(2));
        assertEquals(List.of("G01;Familia Garcia;"), Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void ticketsRestoredAndPriceResetAfterRestart() throws Exception {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], String.join("\n", ANA, "SYSTEM DATE;2026-10-01", "SYSTEM PRICE;60.00",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "EXIT") + "\n", users);
        assertEquals("OK TICKET CREATED -> 60.00", first.out().get(3));
        Run second = run(new String[0], String.join("\n", "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T002;ana@correo.com;2026-10-20", "USER DELETE;ana@correo.com;Clave1!",
                "SYSTEM DATE;2026-10-21", "USER DELETE;ana@correo.com;Clave1!", "EXIT") + "\n", users);
        assertEquals("OK TICKET CREATED -> 40.00", second.out().get(1));
        assertTrue(second.out().get(2).startsWith("ERROR: "), "restored future tickets block deletion");
        assertEquals("OK: User deleted", second.out().get(4));
        assertEquals(List.of("T001;12345678Z;2026-10-15;60.00", "T002;12345678Z;2026-10-20;40.00"),
                Files.readAllLines(dir.resolve("tickets.txt")));
        Run third = run(new String[0], "EXIT\n", users);
        assertEquals(0, third.status());
        assertEquals(2, Files.readAllLines(dir.resolve("tickets.txt")).size(), "history retained after restart");
    }

    @Test
    void groupTicketLockRestoredAfterRestart() throws Exception {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], String.join("\n", ANA, PEDRO, "SYSTEM DATE;2026-10-01",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15", "EXIT") + "\n", users);
        assertEquals("OK GROUP TICKET CREATED -> 80.00", first.out().get(6));
        Run second = run(new String[0], String.join("\n", "SYSTEM DATE;2026-10-10", "GROUP REMOVE;G01;ana@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "SYSTEM DATE;2026-10-16", "GROUP REMOVE;G01;ana@correo.com",
                "EXIT") + "\n", users);
        assertTrue(second.out().get(1).startsWith("ERROR: "), second.out().toString());
        assertTrue(second.out().get(2).startsWith("ERROR: "), second.out().toString());
        assertEquals("OK: User removed from group", second.out().get(4));
        assertEquals(List.of("TG01;G01;2026-10-15;40.00;80.00;TG01-1,TG01-2"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }
}
