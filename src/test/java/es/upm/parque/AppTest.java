package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void startWithStdin(String input, String... args) {
        App.start(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out,
                dir.resolve("users.txt"));
    }

    @Test
    void printsOneResponsePerNonBlankLine() throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserRepository(dir.resolve("u.txt"))),
                new SystemDate(LocalDate.of(2026, 1, 1)));
        String input = CommandProcessorTest.CREATE_ANA + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nUSER FLY\n";
        App.run(new BufferedReader(new StringReader(input)), out, processor);
        assertEquals(String.join(NL, "OK USER CREATED", "OK USER UPDATED", "ERROR UNKNOWN COMMAND", ""), output());
    }

    @Test
    void interactiveModeStopsAtExit() {
        startWithStdin("HELP\nEXIT\n" + CommandProcessorTest.CREATE_ANA + "\n");
        assertTrue(output().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(output().endsWith("OK EXIT" + NL));
        assertFalse(output().contains("OK USER CREATED"));
    }

    @Test
    void interactiveModeEndsWithInput() {
        startWithStdin(CommandProcessorTest.CREATE_ANA + "\n");
        assertEquals("OK USER CREATED" + NL, output());
    }

    @Test
    void fileModeRunsCommandsInOrderAndStopsAtExit() throws IOException {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, List.of(CommandProcessorTest.CREATE_ANA, "", CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "   ", "EXIT",
                "USER DELETE;ana@correo.com;Clave1!"));
        startWithStdin("HELP\n", commands.toString());
        assertEquals(String.join(NL, "OK USER CREATED", "OK USER CREATED", "OK USER UPDATED", "OK EXIT", ""),
                output());
        List<String> stored = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).contains(";699999999;"));
    }

    @Test
    void missingFileIsReportedWithoutException() {
        String missing = dir.resolve("no_existe.txt").toString();
        startWithStdin("", missing);
        assertEquals("ERROR CANNOT READ FILE " + missing + NL, output());
    }

    @Test
    void unreadableFileIsReported() throws IOException {
        Path folder = Files.createDirectory(dir.resolve("folder"));
        startWithStdin("", folder.toString());
        assertEquals("ERROR CANNOT READ FILE " + folder + NL, output());
    }

    @Test
    void invalidUsersFileLocationIsReported() throws IOException {
        Path blocked = Files.createDirectory(dir.resolve("users-as-dir"));
        App.start(new String[0], new ByteArrayInputStream(new byte[0]), out, blocked);
        assertTrue(output().startsWith("ERROR "));
    }
}
