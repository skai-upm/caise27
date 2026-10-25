package es.upm.etsisi.park.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.persistence.UserFileRepository;
import es.upm.etsisi.park.service.UserService;
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
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        service = new UserService(new UserFileRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(service);
    }

    @Test
    void fullUserLifecycle() {
        assertEquals("USER CREATED", processor.process(CREATE_ANA).output());
        assertEquals("USER CREATED", processor.process(CREATE_PEDRO).output());
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").output());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").output());
        assertEquals("USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#").output());
        assertTrue(service.findByEmail("ana.nueva@correo.com").isPresent());
        assertFalse(service.findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void keywordsAreCaseInsensitiveAndTrimmed() {
        assertEquals("USER CREATED", processor.process(
                "  user   create ; Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456").output());
        assertTrue(processor.process("help").output().contains("USER DELETE"));
    }

    @Test
    void unknownCommandIsReported() {
        CommandResult result = processor.process("FOO BAR;1;2");
        assertTrue(result.output().startsWith("ERROR"));
        assertTrue(result.output().contains("Unknown command"));
        assertFalse(result.exit());
    }

    @Test
    void wrongArgumentCountShowsUsage() {
        CommandResult result = processor.process("USER DELETE;ana@correo.com");
        assertTrue(result.output().startsWith("ERROR"));
        assertTrue(result.output().contains("USER DELETE;<email>;<password>"));
        assertTrue(processor.process("USER CREATE;Ana").output().startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;a;b;c").output().startsWith("ERROR"));
    }

    @Test
    void rejectedOperationsAreErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).output().startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!").output().startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output()
                .contains("not modifiable"));
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;x@correo.com;Clave1!;612345678;1234567890123456")
                .output().contains("DNI"));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP").output();
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
        assertFalse(processor.process("HELP").exit());
    }

    @Test
    void blankLineProducesNoOutput() {
        assertEquals("", processor.process("   ").output());
        assertEquals("", processor.process(null).output());
    }
}
