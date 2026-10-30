package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.UserController;
import es.upm.etsisi.amusementpark.repository.UserFileStore;
import es.upm.etsisi.amusementpark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserRepository repository;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        repository = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        processor = new CommandProcessor(new UserController(repository),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createUpdateDelete() {
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "USER UPDATED", "USER DELETED", ""), output());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void ruleViolationPrintsError() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(output().contains("ERROR: DNI already registered"));
        assertEquals(1, repository.findAll().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "FOO;bar",
            "USER",
            "USER CREATE;Ana;Garcia",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
            "USER DELETE;ana@correo.com",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE",
            "HELP;now",
            ";;;"})
    void malformedInputKeepsRunning(String line) {
        assertTrue(processor.process(line));
        assertTrue(output().startsWith("ERROR: "), output());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(out.contains("USER DELETE;<email>;<password>"));
        assertTrue(out.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
