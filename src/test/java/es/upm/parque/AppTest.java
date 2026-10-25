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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the interactive, file and --fly execution modes.
 */
public class AppTest {

    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT",
            "USER DELETE;pedro@correo.com;Otra2#");

    @TempDir
    Path dir;

    private String runApp(String[] args, String stdin, Path baseDir, int expectedCode) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        int code = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), out, baseDir,
                TestContext.FAST_ENCODER);
        assertEquals(expectedCode, code);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Path users(Path baseDir) {
        return baseDir.resolve("data").resolve("users.txt");
    }

    @Test
    void fileModeRunsCommandsInOrderAndStopsAtExit() throws IOException {
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, COMMANDS);

        String output = runApp(new String[] {input.toString()}, "", dir, 0);

        assertTrue(output.contains("OK USER CREATED ana@correo.com"));
        assertTrue(output.contains("OK USER CREATED pedro@correo.com"));
        assertTrue(output.contains("OK USER UPDATED PHONE"));
        assertTrue(output.contains("USER DELETE;Correo;Contraseña"));
        assertFalse(output.contains("USER DELETED"));
        List<String> lines = Files.readAllLines(users(dir));
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("699999999"));
    }

    @Test
    void interactiveModeProducesSameResultsAsFileMode() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, COMMANDS);
        String fileOutput = runApp(new String[] {input.toString()}, "", dir.resolve("a"), 0);
        String interactiveOutput = runApp(new String[0], COMMANDS, dir.resolve("b"), 0);

        assertTrue(interactiveOutput.endsWith(fileOutput));
        assertEquals(withoutPasswordHashes(users(dir.resolve("a"))), withoutPasswordHashes(users(dir.resolve("b"))));
    }

    /**
     * User lines without the salted password hash, which differs between runs.
     */
    private static List<String> withoutPasswordHashes(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            String[] fields = line.split(";");
            fields[4] = "";
            lines.add(String.join(";", fields));
        }
        return lines;
    }

    @Test
    void interactiveModeEndsWhenInputEnds() {
        String output = runApp(new String[0], "HELP\n", dir, 0);
        assertTrue(output.contains("Available commands"));
    }

    @Test
    void missingFileIsReportedInAControlledWay() {
        String output = runApp(new String[] {dir.resolve("missing.txt").toString()}, "", dir, 1);
        assertTrue(output.startsWith("ERROR Cannot read command file"));
    }

    @Test
    void usersPersistAcrossRuns() {
        runApp(new String[0], "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"
                + "1234567890123456\nEXIT\n", dir, 0);
        String output = runApp(new String[0], "USER DELETE;ana@correo.com;Clave1!\n", dir, 0);
        assertTrue(output.contains("OK USER DELETED ana@correo.com"));
    }

    @Test
    void flyModeLeavesPermanentDataUntouchedAndRemovesItsDirectory() throws IOException {
        runApp(new String[0], "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"
                + "1234567890123456\n", dir, 0);
        List<String> before = Files.readAllLines(users(dir));
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, String.join("\n",
                "USER DELETE;ana@correo.com;Clave1!",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "GROUP CREATE;G01;Familia",
                "ATTRACTION CREATE;A01;Tornado;20",
                "EXIT"));

        String output = runApp(new String[] {"--fly", input.toString()}, "", dir, 0);

        assertTrue(output.startsWith("Fly mode: using temporary directory"));
        assertTrue(output.contains("ERROR Unknown email or wrong password"), "fly run must start without users");
        assertTrue(output.contains("OK USER CREATED pedro@correo.com"));
        assertEquals(before, Files.readAllLines(users(dir)));
        assertFalse(Files.exists(dir.resolve("data").resolve("groups.txt")));
        try (Stream<Path> leftovers = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, leftovers.count());
        }
    }

    @Test
    void flyModeWorksInteractivelyAndAfterFileErrors() throws IOException {
        String output = runApp(new String[] {"--fly"}, "HELP\nEXIT\n", dir, 0);
        assertTrue(output.contains("Available commands"));
        runApp(new String[] {"--fly", dir.resolve("missing.txt").toString()}, "", dir, 1);
        try (Stream<Path> leftovers = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, leftovers.count());
        }
    }

    @Test
    void flyDirectoryNamesAreTimestampedAndUnique() throws IOException {
        Path first = App.createFlyDirectory(dir.resolve("tmp"));
        Path second = App.createFlyDirectory(dir.resolve("tmp"));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        assertFalse(first.equals(second));
    }

    @Test
    void rejectsTooManyArguments() {
        String output = runApp(new String[] {"a.txt", "b.txt"}, "", dir, 1);
        assertTrue(output.startsWith("ERROR Too many arguments"));
    }
}
