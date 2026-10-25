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
        App.start(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out, dir);
    }

    @Test
    void printsOneResponsePerNonBlankLine() throws IOException {
        CommandProcessor processor = new Park(dir, LocalDate.of(2026, 1, 1)).processor;
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
    void groupsAreRestoredOnRestart() throws IOException {
        startWithStdin(CommandProcessorTest.CREATE_ANA + "\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\n");
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        bytes.reset();
        startWithStdin("GROUP ADD;G01;ana@correo.com\n");
        assertEquals("ERROR USER ALREADY IN GROUP" + NL, output());
    }

    @Test
    void ticketsAreRestoredOnRestart() throws IOException {
        startWithStdin(CommandProcessorTest.CREATE_ANA + "\nTICKET CREATE;T001;ana@correo.com;2099-10-15\n");
        assertEquals(List.of("T001;2099-10-15;12345678Z;40.00"), Files.readAllLines(dir.resolve("tickets.txt")));
        bytes.reset();
        startWithStdin("TICKET CREATE;T001;ana@correo.com;2099-10-16\nUSER DELETE;ana@correo.com;Clave1!\n");
        assertEquals("ERROR DUPLICATE TICKET" + NL + "ERROR USER HAS ACTIVE TICKETS" + NL, output());
    }

    @Test
    void groupTicketsAreRestoredOnRestart() throws IOException {
        startWithStdin(CommandProcessorTest.CREATE_ANA + "\nGROUP CREATE;G01;Familia\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUPTICKET CREATE;TG01;G01;2099-10-15\n");
        assertEquals(List.of("TG01;2099-10-15;G01;0;12345678Z:40.00"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
        bytes.reset();
        startWithStdin("GROUP DELETE;G01\n");
        assertEquals("ERROR GROUP LOCKED" + NL, output());
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
        Path blocked = Files.createDirectories(dir.resolve("blocked").resolve("users.txt")).getParent();
        App.start(new String[0], new ByteArrayInputStream(new byte[0]), out, blocked);
        assertTrue(output().startsWith("ERROR "));
    }
}
