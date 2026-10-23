package es.upm.park.cli;

import es.upm.park.persistence.UserFileRepository;
import es.upm.park.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserService(new UserFileRepository(dir.resolve("users.txt"))));
    }

    @Test
    void createsUser() {
        CommandResult result = processor.process(CREATE_ANA);
        assertEquals("USER CREATED", result.output());
        assertFalse(result.exit());
    }

    @Test
    void commandNameIsCaseInsensitiveAndFieldsAreTrimmed() {
        assertEquals("USER CREATED", processor.process(
                "  user   create ; Ana ;Garcia Lopez;12345678Z; ana@correo.com ;Clave1!;612345678;1234567890123456")
                .output());
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;phone;699999999")
                .output());
    }

    @Test
    void updatesAndDeletesUser() {
        processor.process(CREATE_ANA);
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").output());
        assertEquals("USER DELETED", processor.process("USER DELETE;ana.nueva@correo.com;Clave1!").output());
    }

    @Test
    void rejectedOperationsReportErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).output().startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Wrong1!").output().startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output()
                .startsWith("ERROR:"));
    }

    @Test
    void unknownCommandIsAnError() {
        CommandResult result = processor.process("FOO BAR");
        assertTrue(result.output().startsWith("ERROR: Unknown command"));
        assertFalse(result.exit());
    }

    @Test
    void wrongFieldCountShowsSyntax() {
        CommandResult result = processor.process("USER DELETE;ana@correo.com");
        assertEquals("ERROR: Usage: " + CommandProcessor.USER_DELETE_SYNTAX, result.output());
        assertTrue(processor.process("USER CREATE;Ana").output().contains(CommandProcessor.USER_CREATE_SYNTAX));
        assertTrue(processor.process("USER UPDATE;a;b;c").output().contains(CommandProcessor.USER_UPDATE_SYNTAX));
        assertTrue(processor.process("HELP;extra").output().startsWith("ERROR:"));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP").output();
        assertTrue(help.contains(CommandProcessor.USER_CREATE_SYNTAX));
        assertTrue(help.contains(CommandProcessor.USER_DELETE_SYNTAX));
        assertTrue(help.contains(CommandProcessor.USER_UPDATE_SYNTAX));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitRequestsTermination() {
        CommandResult result = processor.process("exit");
        assertTrue(result.exit());
    }

    @Test
    void blankLineProducesNoOutput() {
        assertEquals("", processor.process("   ").output());
    }

    @Test
    void normalizesCommandName() {
        assertEquals("USER CREATE", CommandProcessor.normalizeCommandName("  user \t create "));
    }
}
