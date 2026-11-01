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
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppTest {

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER DELETE;pedro@correo.com;Wrong1!",
            "BOGUS",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private String runApp(String[] args, InputStream in, Path workDir) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, in, new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static InputStream input(List<String> lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }

    private Path commandFile(List<String> lines) throws IOException {
        Path file = Files.createTempFile(dir, "input", ".txt");
        Files.write(file, lines);
        return file;
    }

    private static boolean isEmptyDir(Path d) throws IOException {
        try (Stream<Path> s = Files.list(d)) {
            return s.findAny().isEmpty();
        }
    }

    @Test
    public void interactiveAndFileModesGiveSameResults() throws IOException {
        Path interactiveDir = Files.createDirectory(dir.resolve("interactive"));
        Path fileDir = Files.createDirectory(dir.resolve("file"));

        String interactiveOut = runApp(new String[0], input(COMMANDS), interactiveDir);
        String fileOut = runApp(new String[] {commandFile(COMMANDS).toString()}, noInput(), fileDir);

        assertEquals(interactiveOut, fileOut);
        assertEquals(Files.readAllLines(interactiveDir.resolve("users.txt")),
                Files.readAllLines(fileDir.resolve("users.txt")));
        assertTrue(Files.readString(fileDir.resolve("users.txt")).contains("699999999"));
    }

    @Test
    public void missingCommandFileIsReportedWithoutException() {
        String out = runApp(new String[] {dir.resolve("nope.txt").toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error: command file not found or not readable"));
    }

    @Test
    public void directoryAsCommandFileIsReported() {
        String out = runApp(new String[] {dir.toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error:"));
    }

    @Test
    public void usersAreRestoredOnNextExecution() throws IOException {
        runApp(new String[] {commandFile(List.of(COMMANDS.get(0), "EXIT")).toString()}, noInput(), dir);
        String out = runApp(new String[0], input(List.of(COMMANDS.get(0), "EXIT")), dir);
        assertTrue(out.contains("already exists"));
    }

    // --- --fly

    @Test
    public void flyRunDoesNotTouchPermanentDataAndCleansUp() throws IOException {
        runApp(new String[0], input(List.of(COMMANDS.get(0), "EXIT")), dir);
        String before = Files.readString(dir.resolve("users.txt"));

        String out = runApp(new String[] {"--fly"}, input(List.of(
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Changed",
                COMMANDS.get(1),
                "GROUP CREATE;G01;Familia",
                "ATTRACTION CREATE;A01;Tornado;20",
                "EXIT")), dir);

        assertTrue(out.contains("Error: Incorrect email or password"), "fly run starts with empty storage");
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
    }

    @Test
    public void flyWithCommandFileAndEndOfInputCleansUp() throws IOException {
        Path file = commandFile(List.of(COMMANDS.get(0), "GROUP CREATE;G01;Familia"));
        String out = runApp(new String[] {"--fly", file.toString()}, noInput(), dir);
        assertTrue(out.contains("User created"));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    public void flyWithMissingFileCleansUp() throws IOException {
        String out = runApp(new String[] {"--fly", dir.resolve("nope.txt").toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error: command file not found"));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
    }

    @Test
    public void flyAndNormalRepliesAreIdentical() throws IOException {
        Path normalDir = Files.createDirectory(dir.resolve("normal"));
        Path flyDir = Files.createDirectory(dir.resolve("fly"));
        List<String> script = List.of(COMMANDS.get(0), "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "USER DELETE;ana@correo.com;Clave1!", "EXIT");
        String normal = runApp(new String[0], input(script), normalDir);
        String fly = runApp(new String[] {"--fly"}, input(script), flyDir);
        assertEquals(normal, fly);
        assertTrue(normal.contains("OK TICKET CREATED -> 40.00"));
        assertTrue(Files.exists(normalDir.resolve("tickets.txt")));
        assertTrue(isEmptyDir(flyDir.resolve("tmp")));
    }
}
