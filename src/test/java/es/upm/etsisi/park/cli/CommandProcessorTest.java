package es.upm.etsisi.park.cli;

import es.upm.etsisi.park.persistence.UserFileStore;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserRepository repository;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        repository = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        processor = new CommandProcessor(new UserService(repository), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void runsAssignmentExampleSequence() throws IOException {
        String input = String.join("\n", CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT");

        new CommandRunner(processor).run(new BufferedReader(new StringReader(input)));

        String[] lines = output().split("\n");
        assertEquals("USER CREATED", lines[0].trim());
        assertEquals("USER CREATED", lines[1].trim());
        assertEquals("USER UPDATED", lines[2].trim());
        assertEquals("USER UPDATED", lines[3].trim());
        assertEquals("USER DELETED", lines[4].trim());
        assertTrue(output().contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findByEmail("ana.nueva@correo.com").isPresent());
    }

    @Test
    void unknownCommandReportsErrorAndContinues() {
        assertTrue(processor.process("FOO BAR;1"));
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void wrongArgumentCountReportsSyntax() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia"));

        assertTrue(output().contains("Usage: USER CREATE;"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rejectedOperationReportsError() {
        processor.process(CREATE_ANA);
        processor.process("USER DELETE;ana@correo.com;Wrong1!");

        assertTrue(output().contains("ERROR: Incorrect password"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void commandNamesAreCaseInsensitive() {
        processor.process("user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");

        assertTrue(output().contains("USER CREATED"));
    }

    @Test
    void exitStopsProcessing() throws IOException {
        assertFalse(processor.process("EXIT"));

        String input = String.join("\n", CREATE_ANA, "", "   ", "EXIT", CREATE_PEDRO);
        new CommandRunner(processor).run(new BufferedReader(new StringReader(input)));

        assertEquals(1, repository.findAll().size());
        assertFalse(output().contains("ERROR"));
    }
}
