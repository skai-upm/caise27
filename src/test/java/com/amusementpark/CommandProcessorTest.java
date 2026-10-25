package com.amusementpark;

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

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws Exception {
        buffer = new ByteArrayOutputStream();
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void userCommands() {
        assertTrue(processor.process(CREATE_ANA));
        assertEquals(1, manager.getUsers().size());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", manager.getUsers().get(0).getPhone());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertFalse(output().contains("ERROR"), output());
    }

    @Test
    public void unknownCommandIsReported() {
        assertTrue(processor.process("FOO BAR"));
        assertTrue(output().contains("ERROR"));
    }

    @Test
    public void wrongParameterCountIsReported() {
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("USER DELETE;ana@correo.com"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertTrue(processor.process(CREATE_ANA + ";extra"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals(4, output().split("ERROR", -1).length - 1);
    }

    @Test
    public void blankLinesAreIgnored() {
        assertTrue(processor.process(""));
        assertTrue(processor.process("   "));
        assertEquals("", output());
    }

    @Test
    public void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    public void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
