package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
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

    private UserController service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = App.bootstrap(new Storage(dir), LocalDate.of(2027, 4, 15), ParkFixture.FAST_HASHER);
        service = UserController.getInstance();
    }

    private String runAll(String input) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void userCommandsSucceed() {
        assertEquals("USER CREATED", processor.process(CREATE_ANA));
        assertEquals("USER CREATED", processor.process(CREATE_PEDRO));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        assertEquals("699999999", service.findByEmail("ana.nueva@correo.com").getPhone());
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void rejectedOperationsReportErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void malformedAndUnknownCommandsDoNotStopProcessing() {
        assertTrue(processor.process("USER LIST").startsWith("ERROR:"));
        assertTrue(processor.process("FOO").startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR:"));
        assertTrue(processor.process("USER CREATE;Ana").startsWith("ERROR:"));
        assertTrue(processor.process(";;;").startsWith("ERROR:"));
        assertTrue(processor.process("HELP;extra").startsWith("ERROR:"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(help.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(help.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsLaterCommands() throws Exception {
        runAll(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertTrue(processor.isExitRequested());
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertNull(service.findByEmail("pedro@correo.com"));
    }

    @Test
    void runProcessesInOrderAndIgnoresBlankLines() throws Exception {
        String out = runAll("\n" + CREATE_ANA + "\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nFOO\nHELP\n");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER UPDATED", lines[1]);
        assertTrue(lines[2].startsWith("ERROR:"));
        assertEquals("Available commands:", lines[3]);
        assertEquals("699999999", service.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void helpListsAllDeliveryTwoSyntax() {
        String help = processor.process("HELP");
        for (String syntax : new String[] {
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
                "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void deliveryTwoCommandsAreDispatched() {
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        assertEquals("SYSTEM DATE SET: 2027-04-16", processor.process("SYSTEM DATE;2027-04-16"));
        assertEquals("SYSTEM PRICE SET: 45.50 EUR", processor.process("SYSTEM PRICE;45.50"));
        assertEquals("GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("USER ADDED TO GROUP", processor.process("GROUP ADD;G01;pedro@correo.com"));
        assertEquals("USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;pedro@correo.com"));
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", processor.process("TICKET CREATE;T001;pedro@correo.com;2027-04-16"));
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", processor.process("TICKET CREATE;T002;pedro@correo.com;2027-04-20"));
        assertEquals("TICKET DELETED", processor.process("TICKET DELETE;T002"));
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: 45.50 EUR",
                processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-16"));
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: 45.50 EUR",
                processor.process("GROUPTICKET CREATE;TG02;G01;2027-04-20"));
        assertEquals("GROUPTICKET DELETED", processor.process("GROUPTICKET DELETE;TG02"));
        assertEquals("ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("ATTRACTION ENTERED: 1 user(s)", processor.process("ATTRACTION ENTER;A01;TG01"));
        assertEquals("ATTRACTION ENTERED: 1 user(s)", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("ATTRACTION EXITED", processor.process("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals("pedro@correo.com", processor.process("ATTRACTION LIST;A01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR:"));
        processor.process("SYSTEM DATE;2027-04-17");
        assertEquals("GROUP DELETED", processor.process("GROUP DELETE;G01"));
    }

    @Test
    void deliveryTwoArgumentCountsChecked() {
        for (String bad : new String[] {"SYSTEM DATE", "SYSTEM PRICE;1;2", "GROUP CREATE;G01", "GROUP ADD;G01",
                "GROUP REMOVE;G01", "GROUP DELETE", "TICKET CREATE;T1;a@b.com", "TICKET DELETE",
                "GROUPTICKET CREATE;T;G", "GROUPTICKET DELETE", "ATTRACTION CREATE;A;B", "ATTRACTION ENTER;A",
                "ATTRACTION EXIT;A", "ATTRACTION LIST", "ATTRACTION STATUS;A;B"}) {
            assertTrue(processor.process(bad).startsWith("ERROR: wrong number of arguments"), bad);
        }
    }
}
