package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import es.upm.parque.business.model.User;
import es.upm.parque.persistence.FileUserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the execution modes of {@link App}.
 */
class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String run(String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        App.run(args, dir, dir.resolve("tmp"), in, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private List<User> storedUsers() {
        return new FileUserRepository(dir.resolve("users.txt")).load();
    }

    @Test
    void interactiveModeReadsStandardInputUntilExit() {
        String out = run(new String[0], CREATE_ANA + "\nHELP\nEXIT\n" + CREATE_PEDRO + "\n");
        assertTrue(out.startsWith("OK USER CREATED"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertEquals(1, storedUsers().size());
    }

    @Test
    void interactiveModeEndsWhenInputEnds() {
        run(new String[0], CREATE_ANA + "\n");
        assertEquals(1, storedUsers().size());
    }

    @Test
    void fileModeExecutesInOrderIgnoresBlankLinesAndStopsAtExit() throws IOException {
        Path commands = dir.resolve("input_basico.txt");
        Files.writeString(commands, CREATE_ANA + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
                + "EXIT\n" + CREATE_PEDRO + "\n");
        String out = run(new String[] {commands.toString()}, "");
        assertEquals(String.join(System.lineSeparator(), "OK USER CREATED", "OK USER UPDATED", "OK EXIT", ""), out);
        List<User> users = storedUsers();
        assertEquals(1, users.size());
        assertEquals("699999999", users.get(0).getPhone());
    }

    @Test
    void fileAndInteractiveModesGiveTheSameResult() throws IOException {
        String script = CREATE_ANA + "\nUSER DELETE;ana@correo.com;Mala1!\nHELP\n";
        Path commands = dir.resolve("script.txt");
        Files.writeString(commands, script);
        String fromFile = run(new String[] {commands.toString()}, "");
        Files.delete(dir.resolve("users.txt"));
        String fromStdin = run(new String[0], script);
        assertEquals(fromFile, fromStdin);
    }

    @Test
    void missingFileIsReportedWithoutException() {
        String out = run(new String[] {dir.resolve("does-not-exist.txt").toString()}, "");
        assertTrue(out.startsWith("ERROR Cannot read command file"));
        assertTrue(out.contains("file not found"));
    }

    @Test
    void directoryAsFileIsReportedWithoutException() {
        String out = run(new String[] {dir.toString()}, "");
        assertTrue(out.startsWith("ERROR Cannot read command file"));
    }

    @Test
    void usersAreRestoredOnNextExecution() {
        run(new String[0], CREATE_ANA + "\nEXIT\n");
        String out = run(new String[0], "USER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertTrue(out.startsWith("OK USER DELETED"));
        assertTrue(storedUsers().isEmpty());
    }

    @Test
    void flyModeUsesTemporaryDirectoryAndDeletesItAfterExit() throws IOException {
        run(new String[0], CREATE_ANA + "\nEXIT\n");
        String before = Files.readString(dir.resolve("users.txt"));
        Path commands = dir.resolve("input_fase2.txt");
        Files.writeString(commands, CREATE_PEDRO + "\nUSER DELETE;ana@correo.com;Clave1!\nEXIT\n");

        String out = run(new String[] {"--fly", commands.toString()}, "");

        assertEquals(String.join(System.lineSeparator(), "ERROR User not found: ana@correo.com", "OK EXIT", ""),
                out.substring(out.indexOf(System.lineSeparator()) + System.lineSeparator().length()));
        assertTrue(out.startsWith("OK USER CREATED"));
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void flyModeInteractiveKeepsDataWithinTheRun() throws IOException {
        String out = run(new String[] {"--fly"}, CREATE_ANA + "\nUSER DELETE;ana@correo.com;Clave1!\n");
        assertEquals(String.join(System.lineSeparator(), "OK USER CREATED", "OK USER DELETED", ""), out);
        assertTrue(Files.notExists(dir.resolve("users.txt")));
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void flyDirectoryIsTimestampedAndUnique() throws IOException {
        Path first = App.createTempDataDir(dir.resolve("tmp"));
        Path second = App.createTempDataDir(dir.resolve("tmp"));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        assertTrue(Files.isDirectory(first) && Files.isDirectory(second));
        assertTrue(!first.equals(second));
        Files.writeString(first.resolve("users.txt"), "x");
        App.deleteRecursively(first);
        assertTrue(Files.notExists(first));
    }

    @Test
    void flyCleanupHappensAlsoWhenTheFileCannotBeRead() throws IOException {
        String out = run(new String[] {"--fly", dir.resolve("missing.txt").toString()}, "");
        assertTrue(out.startsWith("ERROR Cannot read command file"));
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void invalidArgumentsAreReported() {
        assertTrue(run(new String[] {"--foo"}, CREATE_ANA).startsWith("ERROR Unknown option: --foo"));
        assertTrue(run(new String[] {"a.txt", "b.txt"}, CREATE_ANA).startsWith("ERROR Only one command file"));
        assertTrue(storedUsers().isEmpty());
    }
}
