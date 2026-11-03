package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserService service;
    private ByteArrayOutputStream output;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws UserException {
        service = new UserService(new UserRepository(dir.resolve("users.txt"),
                new PrintStream(new ByteArrayOutputStream())));
        output = new ByteArrayOutputStream();
        processor = new CommandProcessor(service, new PrintStream(output, true));
    }

    private String run(String... lines) throws Exception {
        processor.run(new BufferedReader(new StringReader(String.join("\n", lines))));
        return output.toString();
    }

    @Test
    void createsUser() throws Exception {
        assertTrue(run(CREATE_ANA).contains("USER CREATED"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void reportsMalformedAndUnknownCommandsAndContinues() throws Exception {
        String result = run("USER CREATE;Ana", "FOO BAR", "USER DELETE;x", "USER UPDATE;a;b",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234", CREATE_ANA);
        assertEquals(5, result.split("ERROR:", -1).length - 1);
        assertTrue(result.contains("USER CREATED"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void rejectsDuplicates() throws Exception {
        String result = run(CREATE_ANA,
                "USER CREATE;Otra;X;12345678Z;otra@correo.com;Clave1!;1;1234567890123456",
                "USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;1;1234567890123456");
        assertEquals(2, result.split("ERROR:", -1).length - 1);
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void deletesAndUpdates() throws Exception {
        String result = run(CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER DELETE;pedro@correo.com;Otra2#",
                "USER DELETE;ana@correo.com;Wrong1!");
        assertTrue(result.contains("USER UPDATED"));
        assertTrue(result.contains("USER DELETED"));
        assertTrue(result.contains("ERROR: incorrect password"));
        assertEquals(1, service.getUsers().size());
        assertEquals("699999999", service.getUsers().get(0).getPhone());
    }

    @Test
    void ignoresBlankLines() throws Exception {
        String result = run("", "   ", CREATE_ANA, "");
        assertEquals("USER CREATED" + System.lineSeparator(), result);
    }

    @Test
    void helpListsAllCommands() throws Exception {
        String result = run("HELP");
        assertTrue(result.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(result.contains("USER DELETE;<email>;<password>"));
        assertTrue(result.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(result.contains("HELP"));
        assertTrue(result.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() throws Exception {
        String result = run(CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertTrue(result.contains("BYE"));
        assertEquals(1, service.getUsers().size());
        assertFalse(processor.execute("EXIT"));
        assertTrue(processor.execute("HELP"));
    }

    @Test
    void fileAndReaderProduceSameResult() throws Exception {
        String[] sequence = {CREATE_ANA, "", CREATE_PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER DELETE;pedro@correo.com;Bad1!", "EXIT"};
        String interactive = run(sequence);

        UserService fileService = new UserService(new UserRepository(dir.resolve("file-users.txt"),
                new PrintStream(new ByteArrayOutputStream())));
        ByteArrayOutputStream fileOutput = new ByteArrayOutputStream();
        Path commands = Files.write(dir.resolve("input.txt"), java.util.List.of(sequence));
        new CommandProcessor(fileService, new PrintStream(fileOutput, true)).runFile(commands);

        assertEquals(interactive, fileOutput.toString());
        assertEquals(Files.readAllLines(dir.resolve("users.txt")), Files.readAllLines(dir.resolve("file-users.txt")));
    }
}
