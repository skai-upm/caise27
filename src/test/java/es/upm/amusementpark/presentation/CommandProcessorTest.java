package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import es.upm.amusementpark.AppInitializer;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private UserController service;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        processor = AppInitializer.initialize(dir, new BCryptPasswordHasher(4),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        service = UserController.getInstance();
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(ANA));
        assertEquals("USER CREATED", output().trim());
    }

    @Test
    void reportsErrorsWithoutStopping() {
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("FOO;BAR"));
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(processor.process(""));
        assertTrue(processor.process(";;;"));
        for (String line : output().trim().split(System.lineSeparator())) {
            assertTrue(line.startsWith("ERROR:"), line);
        }
        assertEquals(0, service.getUsers().size());
    }

    @Test
    void deletesAndUpdates() {
        processor.process(ANA);
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        processor.process("USER DELETE;ana@correo.com;Wrong1!");
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        String[] lines = output().trim().split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER UPDATED", lines[1]);
        assertTrue(lines[2].startsWith("ERROR:"));
        assertEquals("USER DELETED", lines[3]);
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(out.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(out.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void helpListsDeliveryTwoCommands() {
        processor.process("HELP");
        String out = output();
        for (String syntax : new String[] {
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
                "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>",
                "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>"}) {
            assertTrue(out.contains(syntax), syntax);
        }
    }

    @Test
    void systemCommands() {
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("SYSTEM DATE;2027-02-30");
        processor.process("SYSTEM PRICE;45.50");
        processor.process("SYSTEM PRICE;-1");
        processor.process(ANA);
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-14");
        String[] lines = output().trim().split(System.lineSeparator());
        assertEquals("SYSTEM DATE SET;2027-04-15", lines[0]);
        assertTrue(lines[1].startsWith("ERROR:"));
        assertEquals("SYSTEM PRICE SET;45.50", lines[2]);
        assertTrue(lines[3].startsWith("ERROR:"));
        assertEquals("TICKET CREATED;PRICE=45.50", lines[5]);
        assertTrue(lines[6].startsWith("ERROR:"));
    }

    @Test
    void deliveryThreeCommands() {
        String today = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).toString();
        processor.process(ANA);
        processor.process("TICKET CREATE;T001;ana@correo.com;" + today);
        processor.process("SPEEDPASS CREATE;SP02;T001;10");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
        processor.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        processor.process("ATTRACTION CREATE;A05;X;15;VIP");
        processor.process("ATTRACTION ENTER;A01;T001;FAST");
        processor.process("ATTRACTION ENTER;A01;T001;SPEED");
        processor.process("ATTRACTION ENTER;A04;T001");
        processor.process("INVOICE CREATE;USER;ana@correo.com");
        processor.process("HELP EXTENDED");
        String[] lines = output().trim().split(System.lineSeparator());
        assertEquals("SPEEDPASS CREATED;10;TOTAL=80.00", lines[2]);
        assertEquals("SPECIALPASS CREATED;PRICE=40.00", lines[3]);
        assertEquals("ATTRACTION CREATED", lines[4]);
        assertEquals("ATTRACTION CREATED", lines[5]);
        assertTrue(lines[6].startsWith("ERROR:"));
        assertTrue(lines[7].startsWith("ERROR:"));
        assertEquals("ATTRACTION ENTERED;1;SPEEDPASS CONSUMED=SP02:1", lines[8]);
        assertEquals("ATTRACTION ENTERED;1;SPECIALPASS CONSUMED=PE01", lines[9]);
        String out = output();
        assertTrue(out.contains("===== INVOICE - USER ====="));
        assertTrue(out.contains("TOTAL: 160.00 EUR"), "40 ticket + 10 x 8 SpeedPasses + 40 Special Pass");
        assertTrue(out.contains("Available commands (extended):"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
