package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createDeleteUpdateCommands() {
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals(String.join(System.lineSeparator(), "OK USER CREATED", "OK USER UPDATED", "OK USER DELETED", ""),
                output());
    }

    @Test
    void rejectedOperationPrintsError() {
        processor.process(CREATE_ANA);
        buffer.reset();
        processor.process("USER DELETE;ana@correo.com;Mala1!");
        assertTrue(output().startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void unknownCommandAndWrongParameterCountDoNotStopProcessing() {
        assertTrue(processor.process("FOO BAR;1"));
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("USER"));
        assertTrue(processor.process("USER DELETE;a@b.com;Clave1!;extra"));
        String[] lines = output().split(System.lineSeparator());
        assertEquals(4, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("ERROR"), line);
        }
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void emptyTrailingFieldIsAnEmptyValueNotAMissingParameter() {
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;");
        assertTrue(output().startsWith("ERROR Invalid card"));
    }

    @Test
    void commandNameIsCaseInsensitive() {
        processor.process(CREATE_ANA.replace("USER CREATE", "user create"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String help = output();
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
