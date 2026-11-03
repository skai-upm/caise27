package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private List<String> runStdin(Path usersFile, String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.runStandardInput(new BufferedReader(new StringReader(String.join("\n", lines))),
                new PrintStream(out, true, StandardCharsets.UTF_8), usersFile);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private List<String> runFile(Path commandFile, Path usersFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.runFile(commandFile, new PrintStream(out, true, StandardCharsets.UTF_8), usersFile);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private Path commandFile(String name, String... lines) throws IOException {
        Path f = dir.resolve(name);
        Files.write(f, List.of(lines));
        return f;
    }

    @Test
    void interactiveSessionStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        List<String> out = runStdin(users, CREATE_ANA, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "HELP", "EXIT", CREATE_PEDRO);
        assertTrue(out.get(0).startsWith("OK:"));
        assertTrue(out.get(1).startsWith("OK:"));
        assertTrue(out.contains("  EXIT"));
        assertEquals("OK: Bye", out.get(out.size() - 1));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(users));
    }

    @Test
    void endOfInputWithoutExitTerminates() throws IOException {
        Path users = dir.resolve("users.txt");
        List<String> out = runStdin(users, CREATE_ANA, "", "NOT A COMMAND", CREATE_PEDRO);
        assertEquals(3, out.size());
        assertTrue(out.get(1).startsWith("ERROR:"));
        assertEquals(2, Files.readAllLines(users).size());
    }

    @Test
    void fileModeRunsSampleInOrder() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = commandFile("input_basico.txt", CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "EXIT");
        List<String> out = runFile(input, users);
        assertEquals(List.of("OK: User ana@correo.com created", "OK: User pedro@correo.com created",
                "OK: User ana@correo.com updated (PHONE)", "OK: Bye"), out);
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(users));
    }

    @Test
    void fileModeSkipsBlankLinesAndStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = commandFile("in.txt", CREATE_ANA, "", "   ", "HELP", "EXIT", CREATE_PEDRO);
        List<String> out = runFile(input, users);
        assertTrue(out.get(0).startsWith("OK:"));
        assertEquals("Available commands:", out.get(1));
        assertEquals("OK: Bye", out.get(out.size() - 1));
        assertFalse(out.stream().anyMatch(l -> l.contains("pedro")));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void missingFileIsReportedInAControlledWay() {
        List<String> out = runFile(dir.resolve("does-not-exist.txt"), dir.resolve("users.txt"));
        assertEquals(1, out.size());
        assertTrue(out.get(0).startsWith("ERROR: Cannot read command file"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void directoryIsReportedInAControlledWay() {
        List<String> out = runFile(dir, dir.resolve("users.txt"));
        assertTrue(out.get(0).startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void undecodableFileIsReportedInAControlledWay() throws IOException {
        Path input = dir.resolve("bad.txt");
        Files.write(input, new byte[] {(byte) 0xC3, (byte) 0x28, '\n'});
        List<String> out = runFile(input, dir.resolve("users.txt"));
        assertTrue(out.get(out.size() - 1).startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void fileAndStandardInputGiveIdenticalResults() throws IOException {
        String[] commands = {CREATE_ANA, "", "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER CREATE;X;Y;12345678A;x@correo.com;Clave1!;6;1234567890123456", CREATE_PEDRO,
            "USER DELETE;pedro@correo.com;Otra2#", "HELP", "USER UPDATE;ana.nueva@correo.com;Clave1!;CARD;9999888877776666",
            "EXIT", CREATE_PEDRO};
        Path usersA = dir.resolve("a.txt");
        Path usersB = dir.resolve("b.txt");
        List<String> stdin = runStdin(usersA, commands);
        List<String> file = runFile(commandFile("cmds.txt", commands), usersB);
        assertEquals(stdin, file);
        assertEquals(Files.readAllLines(usersA), Files.readAllLines(usersB));
    }

    @Test
    void mainWithMissingFileReturnsNormally() {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        try {
            App.main(new String[] {dir.resolve("nope.txt").toString()});
        } finally {
            System.setOut(original);
        }
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void systemDateBehavesTheSameFromFileAndStandardInput() throws IOException {
        String[] commands = {"SYSTEM DATE;2026-09-15", "SYSTEM DATE;2026-02-30", "SYSTEM DATE;2020-01-01", "EXIT"};
        List<String> stdin = runStdin(dir.resolve("a.txt"), commands);
        List<String> file = runFile(commandFile("dates.txt", commands), dir.resolve("b.txt"));
        assertEquals(stdin, file);
        assertEquals(List.of("OK: System date set to 2026-09-15",
                "ERROR: Invalid date 2026-02-30. Expected format yyyy-MM-dd",
                "OK: System date set to 2020-01-01", "OK: Bye"), file);
    }
}
