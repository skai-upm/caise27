package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserManager manager;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        ParkController park = new ParkController(StoragePaths.in(dir));
        manager = park.getUserManager();
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void run(String... lines) throws IOException {
        processor.run(new BufferedReader(new StringReader(String.join("\n", lines))));
    }

    @Test
    void executesUserCommands() {
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", manager.getUsers().get(0).getPhone());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertFalse(output().contains("ERROR"));
    }

    @Test
    void rejectsInsufficientOrExtraParametersWithoutChanges() {
        processor.process("USER CREATE;Ana;Garcia Lopez");
        processor.process(CREATE_ANA + ";extra");
        processor.process("USER DELETE;ana@correo.com");
        processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME");
        assertTrue(manager.getUsers().isEmpty());
        assertEquals(4, output().lines().filter(l -> l.startsWith("ERROR:")).count());
    }

    @Test
    void unknownCommandIsReportedAndProcessingContinues() throws IOException {
        run("USER LIST", "garbage;;;", ";", CREATE_ANA);
        assertEquals(1, manager.getUsers().size());
        assertEquals(3, output().lines().filter(l -> l.startsWith("ERROR:")).count());
    }

    @Test
    void helpListsAllCommands() {
        processor.process("HELP");
        String out = output();
        assertTrue(out.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(out.lines().anyMatch(l -> l.trim().equals("HELP")));
        assertTrue(out.lines().anyMatch(l -> l.trim().equals("EXIT")));
    }

    @Test
    void helpListsEveryF2Command() {
        processor.process("HELP");
        String out = output();
        for (String syntax : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;", "GROUP CREATE;", "GROUP ADD;",
            "GROUP REMOVE;", "GROUP DELETE;", "TICKET CREATE;", "TICKET DELETE;", "GROUPTICKET CREATE;",
            "GROUPTICKET DELETE;", "ATTRACTION CREATE;", "ATTRACTION ENTER;", "ATTRACTION EXIT;",
            "ATTRACTION STATUS;"}) {
            assertTrue(out.contains(syntax), syntax);
        }
    }

    @Test
    void exitStopsProcessing() throws IOException {
        assertFalse(processor.process("EXIT"));
        run(CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertEquals(1, manager.getUsers().size());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @Test
    void blankLinesAreIgnored() throws IOException {
        run(CREATE_ANA, "", "   ", "HELP", "EXIT", "");
        assertEquals(1, manager.getUsers().size());
        assertFalse(output().contains("ERROR"));
    }
}
