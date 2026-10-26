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

import es.upm.parque.presentation.CommandProcessor;

class AppTest {

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
        String input = Fixtures.CREATE_ANA + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nUSER FLY\n";
        App.run(new BufferedReader(new StringReader(input)), out, processor);
        assertEquals(String.join(NL, "OK USER CREATED", "OK USER UPDATED", "ERROR UNKNOWN COMMAND", ""), output());
    }

    @Test
    void interactiveModeStopsAtExit() {
        startWithStdin("HELP\nEXIT\n" + Fixtures.CREATE_ANA + "\n");
        assertTrue(output().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(output().endsWith("OK EXIT" + NL));
        assertFalse(output().contains("OK USER CREATED"));
    }

    @Test
    void interactiveModeEndsWithInput() {
        startWithStdin(Fixtures.CREATE_ANA + "\n");
        assertEquals("OK USER CREATED" + NL, output());
    }

    @Test
    void fileModeRunsCommandsInOrderAndStopsAtExit() throws IOException {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, List.of(Fixtures.CREATE_ANA, "", Fixtures.CREATE_PEDRO,
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
        startWithStdin(Fixtures.CREATE_ANA + "\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\n");
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        bytes.reset();
        startWithStdin("GROUP ADD;G01;ana@correo.com\n");
        assertEquals("ERROR USER ALREADY IN GROUP" + NL, output());
    }

    @Test
    void ticketsAreRestoredOnRestart() throws IOException {
        startWithStdin(Fixtures.CREATE_ANA + "\nTICKET CREATE;T001;ana@correo.com;2099-10-15\n");
        assertEquals(List.of("T001;2099-10-15;12345678Z;40.00"), Files.readAllLines(dir.resolve("tickets.txt")));
        bytes.reset();
        startWithStdin("TICKET CREATE;T001;ana@correo.com;2099-10-16\nUSER DELETE;ana@correo.com;Clave1!\n");
        assertEquals("ERROR DUPLICATE TICKET" + NL + "ERROR USER HAS ACTIVE TICKETS" + NL, output());
    }

    @Test
    void groupTicketsAreRestoredOnRestart() throws IOException {
        startWithStdin(Fixtures.CREATE_ANA + "\nGROUP CREATE;G01;Familia\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUPTICKET CREATE;TG01;G01;2099-10-15\n");
        assertEquals(List.of("TG01;2099-10-15;G01;0;12345678Z:40.00"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
        bytes.reset();
        startWithStdin("GROUP DELETE;G01\n");
        assertEquals("ERROR GROUP LOCKED" + NL, output());
    }

    @Test
    void attractionsAreRestoredEmptyOnRestart() throws IOException {
        startWithStdin("ATTRACTION CREATE;A01;Tornado;20\n");
        assertEquals(List.of("A01;Tornado;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
        bytes.reset();
        startWithStdin("ATTRACTION STATUS;A01\n");
        assertTrue(output().contains("Capacity: 20 | Present: 0 | Available: 20"));
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

    private Path data() {
        return dir.resolve("data");
    }

    private Path tmp() {
        return dir.resolve("tmp");
    }

    private void launch(String input, String... args) {
        App.launch(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out, data(), tmp(),
                Fixtures.START);
    }

    @Test
    void normalLaunchUsesPermanentData() throws IOException {
        launch(Fixtures.CREATE_ANA + "\n");
        assertEquals(1, Files.readAllLines(data().resolve("users.txt")).size());
        assertFalse(Files.exists(tmp()));
    }

    @Test
    void flyModeIsolatesAndDeletesItsData() throws IOException {
        launch(Fixtures.CREATE_ANA + "\n");
        List<String> permanent = Files.readAllLines(data().resolve("users.txt"));
        bytes.reset();
        launch(Fixtures.CREATE_ANA + "\n" + Fixtures.CREATE_PEDRO + "\nEXIT\n", "--fly");
        assertEquals("OK USER CREATED" + NL + "OK USER CREATED" + NL + "OK EXIT" + NL, output());
        assertEquals(permanent, Files.readAllLines(data().resolve("users.txt")));
        assertTrue(Files.isDirectory(tmp()));
        try (var children = Files.list(tmp())) {
            assertEquals(0, children.count());
        }
    }

    @Test
    void flyModeWithCommandFile() throws IOException {
        Path commands = dir.resolve("input_fase2.txt");
        Files.write(commands, List.of(Fixtures.CREATE_ANA, "USER DELETE;ana@correo.com;Clave1!"));
        launch("", "--fly", commands.toString());
        assertEquals("OK USER CREATED" + NL + "OK USER DELETED" + NL, output());
        assertFalse(Files.exists(data()));
        assertFalse(Files.exists(tmp().resolve("20260826_190315")));
    }

    @Test
    void flyModeCleansUpAfterFileErrors() {
        launch("", "no_existe.txt", "--fly");
        assertEquals("ERROR CANNOT READ FILE no_existe.txt" + NL, output());
        assertFalse(Files.exists(tmp().resolve("20260826_190315")));
    }

    @Test
    void reportsTemporaryDirectoryFailure() throws IOException {
        Files.createFile(tmp());
        launch("", "--fly");
        assertEquals("ERROR CANNOT CREATE TEMPORARY DIRECTORY" + NL, output());
    }
}
