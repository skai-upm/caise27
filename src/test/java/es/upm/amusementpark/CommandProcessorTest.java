package es.upm.amusementpark;

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

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private UserService service;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        ParkController park = ParkController.getInstance();
        park.initialize(dir);
        service = park.getUserService();
        processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
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
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }
}
