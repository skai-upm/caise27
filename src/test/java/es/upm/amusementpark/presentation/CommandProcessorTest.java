package es.upm.amusementpark.presentation;

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
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.persistence.StoragePaths;

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ParkController park;
    private UserController manager;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws IOException {
        park = AppBootstrap.create(new StoragePaths(dir), LocalDate.of(2026, 10, 1));
        manager = park.getUsers();
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
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
    public void helpListsEveryF2Command() {
        processor.process("HELP");
        String out = output();
        for (String syntax : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Precio",
            "GROUP CREATE;IdGrupo;Nombre", "GROUP ADD;IdGrupo;Correo", "GROUP REMOVE;IdGrupo;Correo",
            "GROUP DELETE;IdGrupo", "TICKET CREATE;IdTicket;Correo;yyyy-MM-dd", "TICKET DELETE;IdTicket",
            "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd", "GROUPTICKET DELETE;IdTicket",
            "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad", "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION EXIT;IdAtraccion;IdTicket", "ATTRACTION STATUS;IdAtraccion"}) {
            assertTrue(out.contains(syntax), syntax);
        }
    }

    @Test
    public void f2CommandsProduceSpecifiedReplies() throws IOException {
        String script = String.join("\n",
                CREATE_ANA, CREATE_PEDRO,
                "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Marta;Sanz;22222222J;marta@correo.com;Clave1!;600000000;1234567890123456",
                "SYSTEM DATE;2026-10-15",
                "SYSTEM DATE;2026-13-01",
                "SYSTEM PRICE;40.00",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUP ADD;G01;marta@correo.com",
                "GROUP REMOVE;G01;marta@correo.com",
                "GROUP ADD;G01;marta@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "GROUP DELETE;G01",
                "ATTRACTION CREATE;A01;Tornado;20",
                "ATTRACTION ENTER;A01;T001",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION STATUS;A01",
                "ATTRACTION EXIT;A01;T001",
                "ATTRACTION STATUS;A01",
                "TICKET DELETE;T001",
                "GROUPTICKET DELETE;TG01");
        processor.run(new BufferedReader(new StringReader(script)));
        String out = output();
        assertTrue(out.contains("OK TICKET CREATED -> 40.00"));
        assertTrue(out.contains("OK GROUP TICKET CREATED -> 108.00"));
        assertTrue(out.contains("Error: Invalid date (expected yyyy-MM-dd): 2026-13-01"));
        assertTrue(out.contains("Error: Group G01 is locked"));
        assertTrue(out.contains("Users present: 4" + System.lineSeparator() + "Available places: 16"));
        assertTrue(out.contains("Users present: 3" + System.lineSeparator() + "Available places: 17"));
        assertTrue(out.contains("ana@correo.com (Ana Garcia Lopez)"));
        assertTrue(out.contains("Error: Ticket T001 dated 2026-10-15 can no longer be deleted"));
        assertTrue(out.contains("Error: Ticket TG01 dated 2026-10-15 can no longer be deleted"));
    }

    @Test
    public void invoiceCommand() {
        processor.process(CREATE_ANA);
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        processor.process("INVOICE CREATE;USER;ana@correo.com");
        processor.process("INVOICE CREATE;CLIENT;x");
        processor.process("INVOICE CREATE;USER");
        String out = output();
        assertTrue(out.contains("INVOICE USER ana@correo.com - Ana Garcia Lopez"));
        assertTrue(out.contains("TOTAL: 40.00"));
        assertTrue(out.contains("Error: Unknown invoice recipient type"));
        assertTrue(out.contains("Error: INVOICE CREATE expects 2 parameter(s)"));
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
