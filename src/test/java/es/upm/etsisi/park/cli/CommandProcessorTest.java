package es.upm.etsisi.park.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.persistence.UserRepository;
import es.upm.etsisi.park.user.UserService;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        service = new UserService(new UserRepository(dir.resolve("users.txt"), out));
        processor = new CommandProcessor(service, out);
    }

    private String output() {
        String text = buffer.toString(StandardCharsets.UTF_8);
        buffer.reset();
        return text;
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(CREATE_ANA));
        assertEquals("USER CREATED", output().trim());
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void keywordIsCaseAndSpaceInsensitive() {
        processor.process(" user  create ;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertEquals("USER CREATED", output().trim());
    }

    @Test
    void updatesAndDeletes() {
        processor.process(CREATE_ANA);
        output();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals("USER UPDATED", output().trim());
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertEquals("USER DELETED", output().trim());
        assertNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void reportsValidationErrors() {
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(output().startsWith("ERROR: Invalid DNI"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void wrongArgumentCountShowsUsage() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia"));
        String text = output();
        assertTrue(text.startsWith("ERROR: Wrong number of arguments"));
        assertTrue(text.contains("USER CREATE;<firstName>"));
        assertTrue(processor.process("USER DELETE;ana@correo.com"));
        assertTrue(output().startsWith("ERROR"));
        assertTrue(processor.process("HELP;extra"));
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void dniCannotBeUpdated() {
        processor.process(CREATE_ANA);
        output();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        String text = output();
        assertTrue(text.startsWith("ERROR: Field 'DNI' cannot be modified"));
        assertTrue(text.contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void unknownCommandKeepsRunning() {
        assertTrue(processor.process("USER LIST"));
        String text = output();
        assertTrue(text.startsWith("ERROR: Unknown command 'USER LIST'"));
        assertTrue(text.contains("HELP"));
    }

    @Test
    void blankLineIsIgnored() {
        assertTrue(processor.process("   "));
        assertEquals("", output());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("help"));
        String text = output();
        assertTrue(text.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(text.contains("USER DELETE;<email>;<password>"));
        assertTrue(text.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(text.contains("HELP"));
        assertTrue(text.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
