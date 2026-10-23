package es.upm.park;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private Path usersFile;

    @BeforeEach
    void setUp() {
        usersFile = dir.resolve("users.txt");
        System.setProperty(App.USERS_FILE_PROPERTY, usersFile.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(App.USERS_FILE_PROPERTY);
    }

    private int run(String[] args, String stdin) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in,
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    @Test
    void fileModeProcessesCommandsInOrderAndStopsAtExit() throws Exception {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT",
                "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"));

        assertEquals(0, run(new String[]{commands.toString()}, ""));

        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith("USER CREATED" + System.lineSeparator() + "USER CREATED"));
        assertTrue(output.contains("USER UPDATED"));
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("Available commands"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(usersFile));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(new String[0], "HELP\nEXIT\n"));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("Available commands"));
    }

    @Test
    void usersPersistAcrossRuns() {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        out.reset();
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }

    @Test
    void missingCommandFileIsReportedInControlledWay() {
        int status = run(new String[]{dir.resolve("missing.txt").toString()}, "");
        assertEquals(1, status);
        String error = err.toString(StandardCharsets.UTF_8);
        assertTrue(error.contains("missing.txt"));
        assertFalse(error.contains("Exception"));
    }

    @Test
    void directoryAsCommandFileIsRejected() {
        assertEquals(1, run(new String[]{dir.toString()}, ""));
    }
}
