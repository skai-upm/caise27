package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    private UserService service;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        service = new UserService(new UserRepository(dir.resolve("users.txt")));
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(service, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(CREATE_ANA));
        assertEquals("USER CREATED", output().strip());
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void reportsInvalidCreation() {
        assertTrue(processor.process(CREATE_ANA.replace("12345678Z", "12345678A")));
        assertTrue(output().startsWith("ERROR: invalid DNI"));
        assertTrue(service.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER CREATE;Ana;Garcia Lopez;12345678Z",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
            "USER DELETE;ana@correo.com", "USER UPDATE;ana@correo.com;Clave1!;PHONE",
            "HELP;now", "EXIT;now", "USER CREATE"})
    void wrongParameterCountIsReported(String line) {
        assertTrue(processor.process(line));
        assertTrue(output().startsWith("ERROR: wrong number of parameters"), output());
        assertTrue(service.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO;bar", "user create;Ana", "USER", ";", "USER LIST"})
    void unknownCommandIsReported(String line) {
        assertTrue(processor.process(line));
        assertTrue(output().startsWith("ERROR: unknown command"), output());
    }

    @Test
    void deletesUser() {
        processor.process(CREATE_ANA);
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertTrue(output().contains("USER DELETED"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void reportsDeleteWithWrongPassword() {
        processor.process(CREATE_ANA);
        processor.process("USER DELETE;ana@correo.com;Wrong1!");
        assertTrue(output().contains("ERROR: incorrect password"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void updatesUser() {
        processor.process(CREATE_ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertTrue(output().contains("USER UPDATED"));
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void reportsDniUpdate() {
        processor.process(CREATE_ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(output().contains("ERROR: field DNI cannot be modified"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
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
