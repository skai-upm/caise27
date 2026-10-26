package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    @Test
    void createDeleteUpdate() {
        assertEquals("OK USER CREATED", processor.process(ANA));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void operationIsCaseInsensitiveAndTrimmed() {
        assertEquals("OK USER CREATED",
                processor.process(" user create ; Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456 "));
    }

    @Test
    void unknownCommand() {
        String out = processor.process("FOO BAR;1");
        assertTrue(out.startsWith("ERROR Unknown command"), out);
    }

    @Test
    void wrongParameterCount() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER UPDATE;a;b;c").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR Wrong number of parameters"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void trailingEmptyValueIsBlankNotMissing() {
        String out = processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;");
        assertEquals("ERROR The card number is required", out);
    }

    @Test
    void businessErrorsAreReported() {
        processor.process(ANA);
        assertTrue(processor.process(ANA).startsWith("ERROR"));
        assertEquals("ERROR Incorrect password", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("ERROR The DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }

    @Test
    void unexpectedErrorsDoNotPropagate() {
        CommandProcessor broken = new CommandProcessor(null);
        assertTrue(broken.process(ANA).startsWith("ERROR Unexpected error"));
    }
}
