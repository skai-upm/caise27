package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.persistence.UserRepository;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private Path usersFile;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        usersFile = dir.resolve("users.txt");
        System.setProperty(UserRepository.FILE_PROPERTY, usersFile.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(UserRepository.FILE_PROPERTY);
    }

    private int run(InputStream in, String... args) {
        return App.run(args, in,
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Test
    void fileModeProcessesInOrderAndStopsAtExit() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(CREATE_ANA, "", "   ", CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "EXIT",
                "USER DELETE;pedro@correo.com;Otra2#"));

        assertEquals(0, run(noInput(), input.toString()));

        String text = out.toString(StandardCharsets.UTF_8);
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "BYE"), text.lines().toList());
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(usersFile));
    }

    @Test
    void interactiveModeUsesSameProcessing() {
        String commands = CREATE_ANA + "\nHELP\nEXIT\n" + CREATE_PEDRO + "\n";
        assertEquals(0, run(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8))));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("USER CREATED"));
        assertTrue(text.contains("USER DELETE;<email>;<password>"));
        assertTrue(text.trim().endsWith("BYE"));
        assertFalse(text.contains("pedro"));
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        assertEquals(0, run(new ByteArrayInputStream("HELP\n".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void usersPersistBetweenRuns() throws Exception {
        Path first = dir.resolve("first.txt");
        Files.write(first, List.of(CREATE_ANA, "EXIT"));
        run(noInput(), first.toString());

        Path second = dir.resolve("second.txt");
        Files.write(second, List.of("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;ana.nueva@correo.com;Clave1!", "EXIT"));
        out.reset();
        run(noInput(), second.toString());
        assertEquals(List.of("USER UPDATED", "USER DELETED", "BYE"),
                out.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void missingCommandFileIsControlled() {
        assertEquals(1, run(noInput(), dir.resolve("missing.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("missing.txt"));
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void directoryAsCommandFileIsControlled() {
        assertEquals(1, run(noInput(), dir.toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR"));
    }
}
