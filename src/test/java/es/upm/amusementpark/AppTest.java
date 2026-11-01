package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "BAD COMMAND",
            "HELP",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!");

    @TempDir
    Path tempDir;

    private record Outcome(int status, String out, String err) {
    }

    private Outcome run(String[] args, String stdin, Path baseDir) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        int status = App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), baseDir);
        return new Outcome(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveAndFileModesProduceIdenticalResults() throws IOException {
        Path interactiveDir = Files.createDirectory(tempDir.resolve("interactive"));
        Outcome interactive = run(new String[0], String.join("\n", COMMANDS) + "\n", interactiveDir);

        Path commandFile = tempDir.resolve("input_basico.txt");
        Files.write(commandFile, COMMANDS, StandardCharsets.UTF_8);
        Path fileDir = Files.createDirectory(tempDir.resolve("file"));
        Outcome file = run(new String[] {commandFile.toString()}, "", fileDir);

        assertEquals(0, interactive.status());
        assertEquals(0, file.status());
        assertEquals(interactive.out(), file.out());
        assertEquals(Files.readAllLines(interactiveDir.resolve("users.txt")),
                Files.readAllLines(fileDir.resolve("users.txt")));
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws IOException {
        Path commandFile = tempDir.resolve("input.txt");
        Files.write(commandFile, COMMANDS, StandardCharsets.UTF_8);
        run(new String[] {commandFile.toString()}, "", tempDir);

        List<String> stored = Files.readAllLines(tempDir.resolve("users.txt"));
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;"));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        Outcome outcome = run(new String[0], "HELP\n", tempDir);
        assertEquals(0, outcome.status());
        assertTrue(outcome.out().contains("USER DELETE;Correo;Contraseña"));
    }

    @Test
    void usersAreRestoredOnRestart() {
        run(new String[0], COMMANDS.get(0) + "\nEXIT\n", tempDir);
        Outcome second = run(new String[0], "USER DELETE;ana@correo.com;Clave1!\n", tempDir);
        assertTrue(second.out().startsWith("OK:"));
    }

    @Test
    void missingCommandFileIsReportedWithoutStackTrace() {
        Outcome outcome = run(new String[] {tempDir.resolve("nope.txt").toString()}, "", tempDir);
        assertEquals(1, outcome.status());
        assertTrue(outcome.err().startsWith("ERROR: cannot read command file"));
        assertFalse(outcome.err().contains("Exception"));
        assertFalse(outcome.err().contains("\tat "));
    }

    @Test
    void directoryAsCommandFileIsReportedWithoutStackTrace() {
        Outcome outcome = run(new String[] {tempDir.toString()}, "", tempDir);
        assertEquals(1, outcome.status());
        assertTrue(outcome.err().startsWith("ERROR: cannot read command file"));
        assertFalse(outcome.err().contains("\tat "));
    }

    private static List<String> filesUnder(Path dir) throws IOException {
        try (var walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).map(p -> dir.relativize(p) + ":" + read(p)).sorted().toList();
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void flyRunDoesNotTouchPermanentDataAndCleansUp() throws IOException {
        run(new String[0], COMMANDS.get(0) + "\nEXIT\n", tempDir);
        List<String> before = filesUnder(tempDir);

        Outcome fly = run(new String[] {"--fly"}, String.join("\n",
                "USER DELETE;ana@correo.com;Clave1!",
                COMMANDS.get(2),
                "GROUP CREATE;G01;Familia",
                "ATTRACTION CREATE;A01;Tornado;20",
                "EXIT") + "\n", tempDir);

        assertEquals(0, fly.status());
        assertTrue(fly.out().startsWith("ERROR:"), fly.out());
        assertEquals(before, filesUnder(tempDir));
        assertFalse(Files.exists(tempDir.resolve("tmp")));
    }

    @Test
    void flyStorageIsUsedDuringTheRun() throws IOException {
        Path commandFile = tempDir.resolve("input_fase2.txt");
        Files.write(commandFile, List.of(COMMANDS.get(0), "USER DELETE;ana@correo.com;Clave1!", "EXIT"),
                StandardCharsets.UTF_8);
        Path base = Files.createDirectory(tempDir.resolve("base"));
        Outcome fly = run(new String[] {"--fly", commandFile.toString()}, "", base);
        assertEquals(0, fly.status());
        assertTrue(fly.out().contains("OK: user ana@correo.com created"), fly.out());
        assertTrue(fly.out().contains("OK: user ana@correo.com deleted"), fly.out());
        assertFalse(Files.exists(base.resolve("users.txt")));
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void flyOutputEqualsNormalOutput() throws IOException {
        Path commandFile = tempDir.resolve("input.txt");
        Files.write(commandFile, COMMANDS, StandardCharsets.UTF_8);
        Path normalDir = Files.createDirectory(tempDir.resolve("normal"));
        Path flyDir = Files.createDirectory(tempDir.resolve("fly"));
        Outcome normal = run(new String[] {commandFile.toString()}, "", normalDir);
        Outcome fly = run(new String[] {"--fly", commandFile.toString()}, "", flyDir);
        assertEquals(normal.out(), fly.out());
        assertTrue(Files.exists(normalDir.resolve("users.txt")));
        assertFalse(Files.exists(flyDir.resolve("users.txt")));
        assertFalse(Files.exists(flyDir.resolve("tmp")));
    }

    @Test
    void flyCleansUpAtEndOfInputAndOnUnreadableFile() {
        Outcome eof = run(new String[] {"--fly"}, COMMANDS.get(0) + "\n", tempDir);
        assertEquals(0, eof.status());
        assertFalse(Files.exists(tempDir.resolve("tmp")));

        Outcome missing = run(new String[] {"--fly", tempDir.resolve("nope.txt").toString()}, "", tempDir);
        assertEquals(1, missing.status());
        assertTrue(missing.err().startsWith("ERROR: cannot read command file"));
        assertFalse(Files.exists(tempDir.resolve("tmp")));
    }

    @Test
    void temporaryDirectoryIsTimestamped() throws IOException {
        StorageConfig first = StorageConfig.createTemporary(tempDir, LocalDateTime.of(2026, 8, 26, 19, 3, 15));
        StorageConfig second = StorageConfig.createTemporary(tempDir, LocalDateTime.of(2026, 8, 26, 19, 3, 15));
        assertEquals(tempDir.resolve("tmp").resolve("20260826_190315"), first.directory());
        assertEquals(tempDir.resolve("tmp").resolve("20260826_190315_1"), second.directory());
        Files.writeString(first.users(), "x");
        first.deleteDirectory();
        assertFalse(Files.exists(first.directory()));
        assertTrue(Files.exists(tempDir.resolve("tmp")));
        second.deleteDirectory();
        assertFalse(Files.exists(tempDir.resolve("tmp")));
    }
}
