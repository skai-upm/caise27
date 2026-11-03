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

    private List<String> runStdin(Path dataDir, String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.runStandardInput(new BufferedReader(new StringReader(String.join("\n", lines))),
                new PrintStream(out, true, StandardCharsets.UTF_8), dataDir);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private List<String> runFile(Path commandFile, Path dataDir) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.runFile(commandFile, new PrintStream(out, true, StandardCharsets.UTF_8), dataDir);
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
        List<String> out = runStdin(dir, CREATE_ANA, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
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
        List<String> out = runStdin(dir, CREATE_ANA, "", "NOT A COMMAND", CREATE_PEDRO);
        assertEquals(3, out.size());
        assertTrue(out.get(1).startsWith("ERROR:"));
        assertEquals(2, Files.readAllLines(users).size());
    }

    @Test
    void fileModeRunsSampleInOrder() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = commandFile("input_basico.txt", CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "EXIT");
        List<String> out = runFile(input, dir);
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
        List<String> out = runFile(input, dir);
        assertTrue(out.get(0).startsWith("OK:"));
        assertEquals("Available commands:", out.get(1));
        assertEquals("OK: Bye", out.get(out.size() - 1));
        assertFalse(out.stream().anyMatch(l -> l.contains("pedro")));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void missingFileIsReportedInAControlledWay() {
        List<String> out = runFile(dir.resolve("does-not-exist.txt"), dir);
        assertEquals(1, out.size());
        assertTrue(out.get(0).startsWith("ERROR: Cannot read command file"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void directoryIsReportedInAControlledWay() {
        List<String> out = runFile(dir, dir);
        assertTrue(out.get(0).startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void undecodableFileIsReportedInAControlledWay() throws IOException {
        Path input = dir.resolve("bad.txt");
        Files.write(input, new byte[] {(byte) 0xC3, (byte) 0x28, '\n'});
        List<String> out = runFile(input, dir);
        assertTrue(out.get(out.size() - 1).startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void fileAndStandardInputGiveIdenticalResults() throws IOException {
        String[] commands = {CREATE_ANA, "", "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER CREATE;X;Y;12345678A;x@correo.com;Clave1!;6;1234567890123456", CREATE_PEDRO,
            "USER DELETE;pedro@correo.com;Otra2#", "HELP", "USER UPDATE;ana.nueva@correo.com;Clave1!;CARD;9999888877776666",
            "EXIT", CREATE_PEDRO};
        Path usersA = dir.resolve("a");
        Path usersB = dir.resolve("b");
        List<String> stdin = runStdin(usersA, commands);
        List<String> file = runFile(commandFile("cmds.txt", commands), usersB);
        assertEquals(stdin, file);
        assertEquals(Files.readAllLines(usersA.resolve("users.txt")), Files.readAllLines(usersB.resolve("users.txt")));
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
        List<String> stdin = runStdin(dir.resolve("a"), commands);
        List<String> file = runFile(commandFile("dates.txt", commands), dir.resolve("b"));
        assertEquals(stdin, file);
        assertEquals(List.of("OK: System date set to 2026-09-15",
                "ERROR: Invalid date 2026-02-30. Expected format yyyy-MM-dd",
                "OK: System date set to 2020-01-01", "OK: Bye"), file);
    }

    @Test
    void groupsAreRestoredAfterRestart() throws IOException {
        runStdin(dir, CREATE_ANA, CREATE_PEDRO, "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUP REMOVE;G01;pedro@correo.com", "GROUP CREATE;G02;Amigos",
                "GROUP ADD;G02;pedro@correo.com", "EXIT");
        List<String> out = runStdin(dir, "GROUP ADD;G01;ana@correo.com", "GROUP CREATE;G01;Otra",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com", "GROUP REMOVE;G01;ana.nueva@correo.com",
                "GROUP ADD;G01;ana.nueva@correo.com", "USER DELETE;pedro@correo.com;Otra2#", "EXIT");
        assertTrue(out.get(0).startsWith("ERROR:"));  // membership restored: duplicate rejected
        assertTrue(out.get(1).startsWith("ERROR:"));  // group id restored
        assertTrue(out.get(3).startsWith("OK:"));     // restored member is the registered user object
        assertEquals(List.of("G01;Familia Garcia;12345678Z", "G02;Amigos;"), Files.readAllLines(dir.resolve("groups.txt")));
        CommandProcessor reloaded = App.createProcessor(dir);
        assertTrue(reloaded.process("GROUP ADD;G01;ana.nueva@correo.com").startsWith("ERROR:"));
    }

    @Test
    void malformedGroupCommandsAreRejected() {
        List<String> out = runStdin(dir, "GROUP ADD;G01", "GROUP DELETE", "GROUP CREATE;G01;A;B", "GROUP REMOVE;G01");
        assertEquals(4, out.size());
        assertTrue(out.stream().allMatch(l -> l.startsWith("ERROR:")));
        assertTrue(Files.notExists(dir.resolve("groups.txt")));
    }

    @Test
    void ticketsAreRestoredAfterRestartIncludingHistory() throws IOException {
        runStdin(dir, CREATE_ANA, CREATE_PEDRO, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SYSTEM PRICE;60.00", "TICKET CREATE;T002;pedro@correo.com;2026-10-02", "EXIT");
        List<String> out = runStdin(dir, "SYSTEM DATE;2026-10-01", "USER DELETE;ana@correo.com;Clave1!",
                "TICKET CREATE;T001;ana@correo.com;2026-10-20", "TICKET CREATE;T003;ana@correo.com;2026-10-20",
                "SYSTEM DATE;2026-10-03", "USER DELETE;pedro@correo.com;Otra2#", "EXIT");
        assertTrue(out.get(1).startsWith("ERROR:"));                 // restored ticket still blocks deletion
        assertTrue(out.get(2).startsWith("ERROR:"));                 // restored id is unique
        assertEquals("OK TICKET CREATED -> 40.00", out.get(3));      // price is not persisted
        assertTrue(out.get(5).startsWith("OK:"));                    // past ticket does not block
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00", "T002;87654321X;2026-10-02;60.00",
                "T003;12345678Z;2026-10-20;40.00"), Files.readAllLines(dir.resolve("tickets.txt")));
        // third run: the historical ticket of the deleted user is kept unchanged
        runStdin(dir, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T004;ana@correo.com;2026-10-21", "EXIT");
        assertTrue(Files.readAllLines(dir.resolve("tickets.txt")).contains("T002;87654321X;2026-10-02;60.00"));
    }

    @Test
    void groupTicketsSurviveRestartAndRestrictionsFollowTheDate() throws IOException {
        runStdin(dir, CREATE_ANA, CREATE_PEDRO, "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "SYSTEM DATE;2026-10-01", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "EXIT");
        List<String> out = runStdin(dir, "SYSTEM DATE;2026-10-01", "GROUP DELETE;G01", "USER DELETE;pedro@correo.com;Otra2#",
                "GROUPTICKET CREATE;TG01;G01;2026-10-20", "SYSTEM DATE;2026-10-16", "GROUP REMOVE;G01;pedro@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "EXIT");
        assertTrue(out.get(1).startsWith("ERROR:"));
        assertTrue(out.get(2).startsWith("ERROR:"));
        assertTrue(out.get(3).startsWith("ERROR:"));
        assertTrue(out.get(5).startsWith("OK:"));
        assertTrue(out.get(6).startsWith("OK:"));
        assertEquals(List.of("TG01;G01;2026-10-15;80.00;40.00;12345678Z,87654321X"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void attractionsStartEmptyAfterRestart() throws IOException {
        Path input = commandFile("park.txt", CREATE_ANA, "SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001", "ATTRACTION STATUS;A01", "EXIT");
        List<String> out = runFile(input, dir);
        assertTrue(out.contains("Users present: 1"));
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        List<String> again = runStdin(dir, "ATTRACTION STATUS;A01", "EXIT");
        assertTrue(again.contains("Users present: 0"));
        assertTrue(again.contains("Available places: 20"));
    }
}
