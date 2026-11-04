package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserFileStore(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    @Test
    void createsUserFromExampleCommand() {
        Result r = processor.process(CREATE_ANA);
        assertTrue(r.success(), r.message());
        assertTrue(r.toString().startsWith("OK: "));
        assertEquals("Garcia Lopez", manager.getUsers().get(0).getSurnames());
    }

    @Test
    void runsSourceUpdateExamplesInOrder() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").success());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").success());
        User u = manager.getUsers().get(0);
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("9999888877776666", u.getCardNumber());
    }

    @Test
    void deleteExample() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void dniIsNotAnEditableField() {
        processor.process(CREATE_ANA);
        Result r = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertFalse(r.success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "FOO",
        "USER",
        "USER LIST",
        "USER CREATE;Ana",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;a@b.com",
        "USER DELETE;ana@correo.com;",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        ";;;",
        "   "
    })
    void malformedCommandsAreRejectedSafely(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertTrue(r.toString().startsWith("ERROR: "));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void nullCommandIsRejected() {
        assertFalse(processor.process(null).success());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertTrue(processor.process("  USER  CREATE ; Ana ;Garcia Lopez; 12345678Z ;ana@correo.com;Clave1!;612345678;1234567890123456 ").success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }
}
