package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    static final String CREATE_ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        service = new UserService(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(service);
    }

    private String runAll(String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createCommand() {
        assertEquals("USER CREATED", processor.execute(CREATE_ANA));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void createKeepsSpacesInsideFields() {
        processor.execute(CREATE_ANA);
        assertEquals("Garcia Lopez", service.findByEmail("ana@correo.com").getSurnames());
    }

    @Test
    void duplicateCreateReportsError() {
        processor.execute(CREATE_ANA);
        assertTrue(processor.execute(CREATE_ANA).startsWith("ERROR:"));
        assertEquals(1, service.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER CREATE;Ana;;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER CREATE",
        "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"
    })
    void malformedOrInvalidCreateIsRejected(String line) {
        assertTrue(processor.execute(line).startsWith("ERROR:"));
        assertTrue(service.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO BAR;1;2", "user create;Ana", "USER", "USER LIST", ";;;", "HELP;extra", "EXIT;now"})
    void unknownOrMalformedCommandsAreErrors(String line) {
        assertTrue(processor.execute(line).startsWith("ERROR:"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void deleteCommand() {
        processor.execute(CREATE_ANA);
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR:"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com").startsWith("ERROR:"));
        assertEquals(1, service.getUsers().size());
        assertEquals("USER DELETED", processor.execute("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void updateCommand() {
        processor.execute(CREATE_ANA);
        assertEquals("USER UPDATED", processor.execute("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;PHONE").startsWith("ERROR:"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommandSyntax() {
        String help = processor.execute("HELP");
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void runIgnoresBlankLinesAndKeepsOrder() throws IOException {
        String out = runAll(CREATE_ANA + "\n\n   \n" + CREATE_PEDRO + "\nUSER DELETE;pedro@correo.com;Otra2#\n");
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "USER CREATED", "USER DELETED", ""), out);
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void exitStopsLaterCommands() throws IOException {
        String out = runAll(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "BYE", ""), out);
        assertTrue(processor.isExitRequested());
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void errorsDoNotStopProcessing() throws IOException {
        String out = runAll("NOPE\n" + CREATE_ANA + "\n");
        assertTrue(out.startsWith("ERROR:"));
        assertTrue(out.contains("USER CREATED"));
    }
}
