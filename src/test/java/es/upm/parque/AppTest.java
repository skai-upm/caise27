package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> COMMANDS = List.of(
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "",
        "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
        "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
        "EXIT",
        "USER DELETE;pedro@correo.com;Otra2#");

    @TempDir
    Path dir;

    private String run(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
            new PrintStream(bytes, true, StandardCharsets.UTF_8), usersFile);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileModeExecutesCommandsInOrderUntilExit() throws IOException {
        Path input = Files.write(dir.resolve("input_basico.txt"), COMMANDS);
        Path users = dir.resolve("users.txt");

        String output = run(new String[] {input.toString()}, "", users);

        assertEquals(4, output.lines().count(), output);
        assertFalse(output.contains("ERROR"), output);
        List<String> stored = Files.readAllLines(users);
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).contains(";699999999;"));
    }

    @Test
    void interactiveAndFileModesGiveTheSameResult() throws IOException {
        Path input = Files.write(dir.resolve("input.txt"), COMMANDS);
        String fromFile = run(new String[] {input.toString()}, "", dir.resolve("a.txt"));
        String fromStdin = run(new String[0], String.join("\n", COMMANDS) + "\n", dir.resolve("b.txt"));

        assertEquals(fromFile, fromStdin);
        assertEquals(Files.readAllLines(dir.resolve("a.txt")), Files.readAllLines(dir.resolve("b.txt")));
    }

    @Test
    void interactiveModeEndsWithInput() {
        String output = run(new String[0], "HELP\n", dir.resolve("users.txt"));
        assertTrue(output.startsWith("Available commands"), output);
    }

    @Test
    void nonexistentFileIsReportedWithoutException() {
        Path missing = dir.resolve("no_existe.txt");
        String output = run(new String[] {missing.toString()}, "", dir.resolve("users.txt"));
        assertTrue(output.startsWith("ERROR: Cannot open command file") && output.contains("no_existe.txt"), output);
    }

    @Test
    void unreadableFileIsReportedWithoutException() {
        String output = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertTrue(output.startsWith("ERROR: Cannot"), output);
    }

    @Test
    void groupsAreRestoredOnRestart() throws IOException {
        Path users = dir.resolve("users.txt");
        run(new String[0], COMMANDS.get(0) + "\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\n", users);
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        String output = run(new String[0], "GROUP ADD;G01;ana@correo.com\n", users);
        assertTrue(output.contains("already in group"), output);
    }

    @Test
    void ticketsSurviveRestartAndPriceResets() throws IOException {
        Path users = dir.resolve("users.txt");
        run(new String[0], COMMANDS.get(0) + "\nSYSTEM DATE;2026-10-01\nSYSTEM PRICE;60.00\n"
            + "TICKET CREATE;T001;ana@correo.com;2026-10-15\n", users);
        assertEquals(List.of("T001;2026-10-15;60.00;12345678Z"), Files.readAllLines(dir.resolve("tickets.txt")));
        String output = run(new String[0], "SYSTEM DATE;2026-10-01\nTICKET CREATE;T002;ana@correo.com;2026-10-16\n"
            + "USER DELETE;ana@correo.com;Clave1!\n", users);
        assertTrue(output.contains("OK TICKET CREATED -> 40.00"), output);
        assertTrue(output.contains("cannot be deleted"), output);
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        Path users = dir.resolve("users.txt");
        run(new String[0], COMMANDS.get(0) + "\n" + COMMANDS.get(2) + "\nSYSTEM DATE;2026-10-01\n"
            + "GROUP CREATE;G01;Familia\nGROUP ADD;G01;ana@correo.com\nGROUP ADD;G01;pedro@correo.com\n"
            + "GROUPTICKET CREATE;TG01;G01;2026-10-15\n", users);
        assertEquals(List.of("TG01;2026-10-15;G01;40.00;0;TG01-1:12345678Z,TG01-2:87654321X"),
            Files.readAllLines(dir.resolve("group_tickets.txt")));
        String output = run(new String[0], "SYSTEM DATE;2026-10-01\nGROUP DELETE;G01\n", users);
        assertTrue(output.contains("cannot be modified"), output);
    }
}
