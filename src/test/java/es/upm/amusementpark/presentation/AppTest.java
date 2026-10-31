package es.upm.amusementpark.presentation;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    /** Runs the app with its data directory set to the parent of {@code usersFile}. */
    private int run(String[] args, String stdin, Path usersFile) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), usersFile.getParent());
    }

    private static String lines(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    @Test
    void interactiveSessionRunsUntilExit() throws IOException {
        Path data = dir.resolve("users.txt");

        int status = run(new String[0], lines(CREATE_ANA,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "HELP", "EXIT", CREATE_PEDRO), data);

        assertEquals(0, status);
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(output.contains("Bye."));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(data));
    }

    @Test
    void interactiveSessionEndsAtEndOfInput() {
        int status = run(new String[0], lines(CREATE_ANA), dir.resolve("users.txt"));
        assertEquals(0, status);
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("OK:"));
    }

    @Test
    void invalidInteractiveInputIsRecoverable() throws IOException {
        Path data = dir.resolve("users.txt");
        run(new String[0], lines("FOO BAR", "USER CREATE;Ana", CREATE_ANA), data);
        assertEquals(1, Files.readAllLines(data).size());
    }

    @Test
    void fileModeSkipsBlankLinesAndStopsAtExit() throws IOException {
        Path data = dir.resolve("users.txt");
        Path commands = dir.resolve("input_basico.txt");
        Files.writeString(commands, lines(CREATE_ANA, "", "   ", "HELP", "EXIT", CREATE_PEDRO));

        int status = run(new String[] {commands.toString()}, "", data);

        assertEquals(0, status);
        assertEquals("", err.toString(StandardCharsets.UTF_8));
        List<String> stored = Files.readAllLines(data);
        assertEquals(1, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;"));
    }

    @Test
    void fileModeIgnoresStandardInput() throws IOException {
        Path data = dir.resolve("users.txt");
        Path commands = dir.resolve("commands.txt");
        Files.writeString(commands, lines(CREATE_ANA));

        run(new String[] {commands.toString()}, lines(CREATE_PEDRO), data);

        assertEquals(1, Files.readAllLines(data).size());
    }

    @Test
    void missingFileIsReportedWithoutException() {
        int status = run(new String[] {dir.resolve("missing.txt").toString()}, "", dir.resolve("users.txt"));

        assertEquals(1, status);
        String error = err.toString(StandardCharsets.UTF_8);
        assertTrue(error.startsWith("ERROR: cannot read file"));
        assertFalse(error.contains("Exception"));
    }

    @Test
    void directoryArgumentIsReportedWithoutException() {
        int status = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, status);
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR: cannot read file"));
    }

    @Test
    void bothModesProduceEquivalentResults() throws IOException {
        String script = lines(CREATE_ANA, CREATE_PEDRO, "USER CREATE;Bad;Data;12345678A;x;y;z;w",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER DELETE;pedro@correo.com;Otra2#", "HELP", "EXIT");

        Path interactiveData = dir.resolve("interactive").resolve("users.txt");
        run(new String[0], script, interactiveData);
        String interactiveOutput = out.toString(StandardCharsets.UTF_8);

        out.reset();
        Path fileData = dir.resolve("file").resolve("users.txt");
        Path commands = dir.resolve("commands.txt");
        Files.writeString(commands, script);
        run(new String[] {commands.toString()}, "", fileData);

        assertEquals(interactiveOutput, out.toString(StandardCharsets.UTF_8));
        assertEquals(Files.readAllLines(interactiveData), Files.readAllLines(fileData));
    }

    @Test
    void restartRestoresStoredUsers() throws IOException {
        Path data = dir.resolve("users.txt");
        run(new String[0], lines(CREATE_ANA, "EXIT"), data);

        out.reset();
        run(new String[0], lines(CREATE_ANA, "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"), data);

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith("ERROR:"));
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(data));
    }
}
