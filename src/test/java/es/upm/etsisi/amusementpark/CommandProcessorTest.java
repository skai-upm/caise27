package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        service = new UserService(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(service);
    }

    private String runAll(String input) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void userCommandsSucceed() {
        assertEquals("USER CREATED", processor.process(CREATE_ANA));
        assertEquals("USER CREATED", processor.process(CREATE_PEDRO));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        assertEquals("699999999", service.findByEmail("ana.nueva@correo.com").getPhone());
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void rejectedOperationsReportErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void malformedAndUnknownCommandsDoNotStopProcessing() {
        assertTrue(processor.process("USER LIST").startsWith("ERROR:"));
        assertTrue(processor.process("FOO").startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR:"));
        assertTrue(processor.process("USER CREATE;Ana").startsWith("ERROR:"));
        assertTrue(processor.process(";;;").startsWith("ERROR:"));
        assertTrue(processor.process("HELP;extra").startsWith("ERROR:"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsLaterCommands() throws Exception {
        runAll(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertTrue(processor.isExitRequested());
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void runProcessesInOrderAndIgnoresBlankLines() throws Exception {
        String out = runAll("\n" + CREATE_ANA + "\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nFOO\nHELP\n");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER UPDATED", lines[1]);
        assertTrue(lines[2].startsWith("ERROR:"));
        assertEquals("Available commands:", lines[3]);
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
    }
}
