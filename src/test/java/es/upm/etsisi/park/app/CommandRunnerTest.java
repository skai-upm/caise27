package es.upm.etsisi.park.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.command.CommandProcessor;
import es.upm.etsisi.park.persistence.UserFileRepository;
import es.upm.etsisi.park.service.UserService;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserService service;
    private ByteArrayOutputStream buffer;
    private CommandRunner runner;

    @BeforeEach
    void setUp() {
        service = new UserService(new UserFileRepository(dir.resolve("users.txt")));
        buffer = new ByteArrayOutputStream();
        runner = new CommandRunner(new CommandProcessor(service),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private List<String> runLines(boolean interactive, String... lines) throws IOException {
        runner.run(new BufferedReader(new StringReader(String.join("\n", lines))), interactive);
        return buffer.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void processesCommandsInOrderIgnoringBlankLines() throws IOException {
        List<String> output = runLines(false, CREATE_ANA, "", "   ",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output);
        assertEquals("699999999", service.findByEmail("ana@correo.com").orElseThrow().getPhone());
    }

    @Test
    void stopsAtExit() throws IOException {
        List<String> output = runLines(false, CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertEquals(List.of("USER CREATED", "BYE"), output);
        assertFalse(service.findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void continuesAfterInvalidCommand() throws IOException {
        List<String> output = runLines(false, "NOT A COMMAND", CREATE_ANA);
        assertTrue(output.get(0).startsWith("ERROR"));
        assertEquals("USER CREATED", output.get(1));
    }

    @Test
    void endOfInputEndsRun() throws IOException {
        assertEquals(List.of("USER CREATED"), runLines(false, CREATE_ANA));
    }

    @Test
    void interactiveModePrintsPrompt() throws IOException {
        runLines(true, "EXIT");
        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith("> "));
    }

    @Test
    void appRunsCommandFileAndPersistsAcrossRuns() throws IOException {
        Path users = dir.resolve("app-users.txt");
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "HELP", "EXIT"));
        String previous = System.getProperty(App.USERS_FILE_PROPERTY);
        System.setProperty(App.USERS_FILE_PROPERTY, users.toString());
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int status = App.run(new String[] {input.toString()},
                    new PrintStream(out, true, StandardCharsets.UTF_8),
                    new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(0, status);
            List<String> lines = out.toString(StandardCharsets.UTF_8).lines().toList();
            assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED"),
                    lines.subList(0, 5));
            assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                    Files.readAllLines(users));

            Path second = dir.resolve("second.txt");
            Files.write(second, List.of("USER DELETE;ana.nueva@correo.com;Clave1!"));
            out.reset();
            App.run(new String[] {second.toString()}, new PrintStream(out, true, StandardCharsets.UTF_8),
                    new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals("USER DELETED", out.toString(StandardCharsets.UTF_8).trim());
        } finally {
            if (previous == null) {
                System.clearProperty(App.USERS_FILE_PROPERTY);
            } else {
                System.setProperty(App.USERS_FILE_PROPERTY, previous);
            }
        }
    }

    @Test
    void appReportsMissingCommandFile() {
        String previous = System.getProperty(App.USERS_FILE_PROPERTY);
        System.setProperty(App.USERS_FILE_PROPERTY, dir.resolve("u.txt").toString());
        try {
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int status = App.run(new String[] {dir.resolve("missing.txt").toString()},
                    new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                    new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(1, status);
            String message = err.toString(StandardCharsets.UTF_8);
            assertTrue(message.startsWith("ERROR"));
            assertTrue(message.contains("missing.txt"));
        } finally {
            if (previous == null) {
                System.clearProperty(App.USERS_FILE_PROPERTY);
            } else {
                System.setProperty(App.USERS_FILE_PROPERTY, previous);
            }
        }
    }
}
