package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private UserService service;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        service = new UserService(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(service, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(ANA));
        assertEquals("USER CREATED", output().trim());
    }

    @Test
    void reportsErrorsWithoutStopping() {
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("FOO;BAR"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(processor.process(""));
        assertTrue(processor.process(";;;"));
        for (String line : output().trim().split(System.lineSeparator())) {
            assertTrue(line.startsWith("ERROR:"), line);
        }
        assertEquals(0, service.getUsers().size());
    }

    @Test
    void deletesAndUpdates() {
        processor.process(ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        processor.process("USER DELETE;ana@correo.com;Wrong1!");
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        String[] lines = output().trim().split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER UPDATED", lines[1]);
        assertTrue(lines[2].startsWith("ERROR:"));
        assertEquals("USER DELETED", lines[3]);
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
