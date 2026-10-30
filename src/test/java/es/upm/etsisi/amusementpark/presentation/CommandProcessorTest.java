package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.ApplicationInitializer;
import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
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
        processor = ApplicationInitializer.initialize(dir, ParkFixture.FAST_HASHER,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        park = ParkController.getInstance();
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
            "ATTRACTION ENTER;A01;T1;FAST",
            "HELP EXTENDED;x",
            "INVOICE CREATE;USER",
            "SPEEDPASS CREATE;S;T",
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
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "ATTRACTION EXIT;<attraction>;<email>",
                "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "SPEEDPASS DELETE;<speedPassIdentifier>",
                "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "SPECIALPASS DELETE;<specialPassIdentifier>",
                "INVOICE CREATE;USER;<email>",
                "INVOICE CREATE;GROUP;<groupIdentifier>",
                "HELP",
                "HELP EXTENDED",
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

    @Test
    void helpExtendedDescribesEveryCommand() {
        run("HELP EXTENDED");
        String[] lines = output().split(System.lineSeparator());
        int syntaxLines = 0;
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].startsWith("    ")) {
                syntaxLines++;
                assertTrue(i + 1 < lines.length && lines[i + 1].startsWith("    ") && lines[i + 1].trim().length() > 10,
                        "missing description for " + lines[i]);
            }
        }
        assertEquals(29, syntaxLines);
    }

    @Test
    void delivery3Commands() {
        run(CREATE_ANA, CREATE_PEDRO,
                "SYSTEM DATE;2027-04-15",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SPEEDPASS CREATE;SP01;T001;10",
                "SPEEDPASS CREATE;SP02;T001;3",
                "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SPA;TG01:12345678Z;1",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "ATTRACTION ENTER;A01;TG01;SPEED",
                "ATTRACTION ENTER;A04;T001;SPEED",
                "ATTRACTION ENTER;A04;T001",
                "ATTRACTION STATUS;A04",
                "SPEEDPASS DELETE;SP01:2",
                "INVOICE CREATE;USER;ana@correo.com",
                "INVOICE CREATE;GROUP;G01");
        String out = output();
        assertTrue(out.contains("SPEEDPASS CREATED - 10 pass(es) - TOTAL PRICE: 80.00 EUR"));
        assertTrue(out.contains("ERROR: Quantity must be 1 or 10: 3"));
        assertTrue(out.contains("SPECIALPASS CREATED - PRICE: 40.00 EUR"));
        assertTrue(out.contains("ATTRACTION CREATED (SPECIAL)"));
        assertTrue(out.contains("ERROR: Ticket TG01:87654321X has no unconsumed SpeedPass"));
        assertTrue(out.contains("ERROR: Special attraction A04 does not accept SpeedPasses"));
        assertTrue(out.contains("SPECIALPASS CONSUMED: PE01"));
        assertTrue(out.contains("TYPE: SPECIAL"));
        assertTrue(out.contains("ERROR: Only passes of tickets dated after 2027-04-15 can be refunded"));
        assertTrue(out.contains("INVOICE - USER"));
        assertTrue(out.contains("TOTAL: 168.00 EUR"));
        assertTrue(out.contains("INVOICE - GROUP"));
        assertTrue(out.contains("TOTAL: 80.00 EUR"));
    }
}
