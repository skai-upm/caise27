package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.business.controller.UserManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

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
    void setUp() throws IOException {
        buffer = new ByteArrayOutputStream();
        ParkController park = new ParkController(new StoragePaths(dir), LocalDate.of(2026, 10, 1));
        manager = park.getUsers();
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(CREATE_ANA));
        assertNotNull(manager.findByEmail("ana@correo.com"));
        assertTrue(output().startsWith("OK:"));
    }

    @Test
    void deletesAndUpdatesUser() {
        processor.process(CREATE_ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria");
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());

        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void reportsMissingParametersWithoutCreating() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void reportsExtraParameters() {
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!;extra"));
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void reportsUnknownCommandAndKeepsRunning() {
        assertTrue(processor.process("FOO BAR"));
        assertTrue(output().startsWith("ERROR:"));
        assertTrue(processor.process(CREATE_ANA));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsDniAndUnknownUpdateFields() {
        processor.process(CREATE_ANA);
        buffer.reset();

        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30");

        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
        assertEquals(2, output().lines().filter(l -> l.startsWith("ERROR:")).count());
    }

    @Test
    void reportsRejectedOperations() {
        processor.process(CREATE_ANA);
        buffer.reset();

        processor.process(CREATE_ANA);
        processor.process("USER DELETE;ana@correo.com;Wrong1!");

        assertEquals(2, output().lines().filter(l -> l.startsWith("ERROR:")).count());
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void ignoresBlankLines() {
        assertTrue(processor.process(""));
        assertTrue(processor.process("   "));
        assertEquals("", output());
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
