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
        App.run(args, dir.resolve("users.txt"), in, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private List<User> storedUsers() {
        return new UserRepository(dir.resolve("users.txt")).load();
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
}
