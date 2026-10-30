package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private UserRepository repository;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repository = new UserRepository(dir.resolve("users.txt"));
        processor = new CommandProcessor(new UserService(repository));
    }

    @Test
    void createDeleteUpdateMessages() {
        assertEquals("USER CREATED", processor.process(CREATE_ANA).output());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").output());
        assertEquals("699999999", repository.findByEmail("ana@correo.com").getPhone());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output());
        assertEquals("Ana Maria", repository.findByEmail("ana@correo.com").getFirstName());
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!").output());
        assertNull(repository.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectedOperationsReportErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).output().startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!").output().startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output()
                .startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;COLOR;red").output()
                .startsWith("ERROR:"));
        assertEquals("12345678Z", repository.findByEmail("ana@correo.com").getDni());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO BAR", "USER LIST", "USER", "USER CREATE", ";;;", "USER UPDATE;a;b",
            "USER CREATE;Ana;;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER DELETE;ana@correo.com;Clave1!;extra", "HELP;now", "help", "exit"})
    void malformedLinesAreErrorsAndDoNotExit(String line) {
        CommandProcessor.Result result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
        assertFalse(result.exit());
    }

    @Test
    void helpListsEveryCommand() {
        CommandProcessor.Result result = processor.process("HELP");
        assertFalse(result.exit());
        String help = result.output();
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertEquals("USER CREATED", processor.process("  " + CREATE_ANA + "  ").output());
        assertNotNull(repository.findByEmail("ana@correo.com"));
    }
}
