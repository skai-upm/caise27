package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        park = ParkController.getInstance();
        park.initialize(dir);
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void run(String... lines) {
        for (String line : lines) {
            assertTrue(processor.process(line), line);
        }
    }

    @Test
    void createUpdateDelete() {
        run(CREATE_ANA, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "USER DELETE;ana@correo.com;Clave1!");
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "USER UPDATED", "USER DELETED", ""), output());
        assertTrue(park.users().findAll().isEmpty());
    }

    @Test
    void ruleViolationPrintsError() {
        run(CREATE_ANA, CREATE_ANA);
        assertTrue(output().contains("ERROR: DNI already registered"));
        assertEquals(1, park.users().findAll().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "FOO;bar",
            "USER",
            "USER CREATE;Ana;Garcia",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
            "USER DELETE;ana@correo.com",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE",
            "HELP;now",
            "EXIT;now",
            "SYSTEM DATE",
            "SYSTEM PRICE;1;2",
            "GROUP CREATE;G01",
            "TICKET CREATE;T1;ana@correo.com",
            "ATTRACTION ENTER;A01",
            ";;;"})
    void malformedInputKeepsRunning(String line) {
        assertTrue(processor.process(line));
        assertTrue(output().startsWith("ERROR: "), output());
        assertTrue(park.users().findAll().isEmpty());
    }

    @Test
    void helpListsAllCommands() {
        run("HELP");
        String out = output();
        for (String syntax : new String[]{
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>",
                "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>",
                "SYSTEM PRICE;<Price>",
                "GROUP CREATE;<Identifier>;<Name>",
                "GROUP ADD;<Identifier>;<Email>",
                "GROUP REMOVE;<Identifier>;<Email>",
                "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>",
                "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>",
                "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>",
                "HELP",
                "EXIT"}) {
            assertTrue(out.contains(syntax), syntax);
        }
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    void delivery2Commands() {
        run(CREATE_ANA, CREATE_PEDRO,
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;45.50",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION LIST;A01",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION EXIT;A01;pedro@correo.com",
                "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01",
                "GROUP REMOVE;G01;pedro@correo.com",
                "TICKET CREATE;T002;pedro@correo.com;2027-04-20",
                "TICKET DELETE;T002",
                "GROUP CREATE;G02;Vacio",
                "GROUP DELETE;G02");
        String out = output();
        assertTrue(out.contains("SYSTEM DATE SET TO 2027-04-15"));
        assertTrue(out.contains("SYSTEM PRICE SET TO 45.50 EUR"));
        assertTrue(out.contains("TICKET CREATED - PRICE: 45.50 EUR"));
        assertTrue(out.contains("GROUPTICKET CREATED - TOTAL PRICE: 91.00 EUR"));
        assertTrue(out.contains("NO USERS INSIDE"));
        assertTrue(out.contains("ATTRACTION ENTERED: 2 user(s)"));
        assertTrue(out.contains("ATTRACTION EXITED"));
        assertTrue(out.contains("ATTRACTION: A01"));
        assertTrue(out.contains("NAME: Dragon Rojo"));
        assertTrue(out.contains("CAPACITY: 20"));
        assertTrue(out.contains("INSIDE: 1"));
        assertTrue(out.contains("AVAILABLE: 19"));
        assertTrue(out.contains("USERS: ana@correo.com"));
        assertTrue(out.contains("ERROR: Group G01 is locked by an active group ticket"));
        assertTrue(out.contains("TICKET DELETED"));
        assertTrue(out.contains("GROUP DELETED"));
    }
}
