package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream output;
    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        output = new ByteArrayOutputStream();
        service = new UserService(new UserRepository(dir.resolve("users.txt"),
                new PrintStream(OutputStream.nullOutputStream())));
        processor = new CommandProcessor(service, new PrintStream(output, true));
    }

    private String lastLine() {
        String[] lines = output.toString().split(System.lineSeparator());
        return lines[lines.length - 1];
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(CREATE_ANA));
        assertEquals("USER CREATED", lastLine());
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void reportsInvalidCreate() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsWrongFieldCounts() {
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process(CREATE_ANA + ";extra"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("HELP;x"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void reportsUnknownCommand() {
        assertTrue(processor.process("FOO;bar"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("user create;Ana"));
        assertTrue(lastLine().startsWith("ERROR:"));
    }

    @Test
    void deletesUser() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("USER DELETED", lastLine());
        assertNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void updatesUser() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", lastLine());
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void rejectsDniAndUnknownFields() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30"));
        assertTrue(lastLine().startsWith("ERROR:"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String help = output.toString();
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
        assertFalse(processor.process("  EXIT  "));
    }
}
