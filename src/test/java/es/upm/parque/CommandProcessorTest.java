package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    @Test
    void createDeleteAndUpdateThroughCommands() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void reportsWrongParameterCountAndUnknownCommands() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("USER UPDATE;a;b;NAME").startsWith("ERROR Wrong number"));
        assertTrue(processor.process("FOO BAR;1").startsWith("ERROR Unknown command"));
        assertTrue(processor.process("USER").startsWith("ERROR Unknown command"));
        assertTrue(manager.getUsers().isEmpty());
        assertFalse(processor.isFinished());
    }

    @Test
    void rejectsDniAndUnknownFieldsOnUpdate() {
        processor.process(CREATE_ANA);
        assertEquals("ERROR The DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR Unknown field"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitFinishesProcessing() {
        assertTrue(processor.process("EXIT").startsWith("OK"));
        assertTrue(processor.isFinished());
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
