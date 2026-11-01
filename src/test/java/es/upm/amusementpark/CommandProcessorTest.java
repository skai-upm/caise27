package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserManager manager;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new UserManager(new UserStorage(dir.resolve("users.txt")));
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(manager, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void userCommandsAreDispatched() {
        assertTrue(processor.process(CREATE_ANA));
        assertNotNull(manager.findByEmail("ana@correo.com"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void helpListsTheFiveCommands() {
        assertTrue(processor.process("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    public void exitReturnsFalse() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    public void unknownAndMalformedCommandsReportErrors() {
        assertTrue(processor.process("FOO;bar"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia"));
        assertTrue(processor.process("USER DELETE"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertTrue(processor.process(";;;"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals(6, output().lines().filter(l -> l.startsWith("Error:")).count());
    }

    @Test
    public void runSkipsBlankLinesAndStopsAtExit() throws IOException {
        String script = String.join("\n", CREATE_ANA, "", "   ", "HELP", "EXIT", CREATE_PEDRO);
        processor.run(new BufferedReader(new StringReader(script)));
        assertNotNull(manager.findByEmail("ana@correo.com"));
        assertNull(manager.findByEmail("pedro@correo.com"));
        assertFalse(output().contains("Error:"));
    }

    @Test
    public void runStopsAtEndOfInput() throws IOException {
        processor.run(new BufferedReader(new StringReader(CREATE_ANA + "\n" + CREATE_PEDRO)));
        assertEquals(2, manager.getUsers().size());
    }

    @Test
    public void runContinuesAfterInvalidCommand() throws IOException {
        processor.run(new BufferedReader(new StringReader("FOO;bar\nHELP\n")));
        assertTrue(output().contains("Error: unknown command"));
        assertTrue(output().contains("Available commands:"));
    }
}
