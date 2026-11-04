package es.upm.park;

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

    static final String INPUT_BASICO = String.join("\n",
            CommandProcessorTest.CREATE_ANA,
            CommandRunnerTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "EXIT") + "\n";

    @TempDir
    Path dir;

    /** Result of one application run. */
    record Run(int status, String out, String err) {
    }

    private Run run(String[] args, Path usersFile, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, usersFile, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveSessionStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Run r = run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n" + CommandRunnerTest.CREATE_PEDRO + "\n");
        assertEquals(0, r.status());
        assertTrue(r.out().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(r.out().strip().endsWith("OK: application terminated"));
        List<String> lines = Files.readAllLines(users);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(";699999999;"));
    }

    @Test
    void fileModeRunsInputBasico() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, INPUT_BASICO);
        Run r = run(new String[] {input.toString()}, users, "");
        assertEquals(0, r.status());
        assertEquals("", r.err());
        assertEquals(4, r.out().split("\\R").length);
        List<String> lines = Files.readAllLines(users);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains(";699999999;"));
    }

    @Test
    void fileModeSkipsBlankLinesAndStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, CommandProcessorTest.CREATE_ANA + "\n\nHELP\nEXIT\n" + CommandRunnerTest.CREATE_PEDRO + "\n");
        Run r = run(new String[] {input.toString()}, users, "");
        assertEquals(0, r.status());
        assertFalse(r.out().contains("ERROR"));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void sameCommandsGiveSameResultsInBothModes() throws IOException {
        String commands = CommandProcessorTest.CREATE_ANA + "\nbad command\n" + CommandRunnerTest.CREATE_PEDRO
                + "\nUSER DELETE;pedro@correo.com;Mala1!\nUSER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666\nHELP\n";
        Path usersA = dir.resolve("a.txt");
        Path usersB = dir.resolve("b.txt");
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, commands);
        Run interactive = run(new String[0], usersA, commands);
        Run file = run(new String[] {input.toString()}, usersB, "");
        assertEquals(interactive.out(), file.out());
        assertEquals(Files.readAllLines(usersA), Files.readAllLines(usersB));
    }

    @Test
    void missingFileIsReportedWithoutException() {
        Run r = run(new String[] {dir.resolve("nope.txt").toString()}, dir.resolve("users.txt"), "");
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR") && r.err().contains("nope.txt"), r.err());
        assertFalse(r.err().contains("Exception"));
        assertEquals("", r.out());
    }

    @Test
    void directoryIsReportedWithoutException() {
        Run r = run(new String[] {dir.toString()}, dir.resolve("users.txt"), "");
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR"));
        assertFalse(r.err().contains("Exception"));
    }

    @Test
    void unreadableFileIsReported() throws IOException {
        Path input = dir.resolve("locked.txt");
        Files.writeString(input, "HELP\n");
        boolean changed = input.toFile().setReadable(false);
        if (changed && !Files.isReadable(input)) {
            Run r = run(new String[] {input.toString()}, dir.resolve("users.txt"), "");
            assertEquals(1, r.status());
            assertTrue(r.err().startsWith("ERROR"));
        }
        input.toFile().setReadable(true);
    }
}
