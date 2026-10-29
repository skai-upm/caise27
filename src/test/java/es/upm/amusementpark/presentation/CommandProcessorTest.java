package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.ParkInitializer;
import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.CommandInfo;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.business.controller.Park;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ByteArrayOutputStream output;
    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        output = new ByteArrayOutputStream();
        park = TestParks.initialize(dir, LocalDate.of(2027, 4, 15));
        processor = ParkInitializer.commandProcessor(park, new PrintStream(output, true));
    }

    private String lastLine() {
        String[] lines = output.toString().split(System.lineSeparator());
        return lines[lines.length - 1];
    }

    private void assertOk(String command, String expectedLastLine) {
        assertTrue(processor.process(command));
        assertEquals(expectedLastLine, lastLine(), command);
    }

    private void assertError(String command) {
        assertTrue(processor.process(command));
        assertTrue(lastLine().startsWith("ERROR:"), command + " -> " + lastLine());
    }

    @Test
    void createsUser() {
        assertOk(CREATE_ANA, "USER CREATED");
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void reportsInvalidCreate() {
        assertError("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void rejectsWrongFieldCounts() {
        assertError("USER CREATE;Ana");
        assertError(CREATE_ANA + ";extra");
        assertError("USER DELETE;ana@correo.com");
        assertError("USER UPDATE;ana@correo.com;Clave1!;NAME");
        assertError("HELP;x");
        assertError("SYSTEM DATE");
        assertError("SYSTEM PRICE;1;2");
        assertError("GROUP CREATE;G01");
        assertError("GROUP ADD;G01");
        assertError("GROUP REMOVE;G01");
        assertError("GROUP DELETE");
        assertError("TICKET CREATE;T001;ana@correo.com");
        assertError("TICKET DELETE");
        assertError("GROUPTICKET CREATE;TG01;G01");
        assertError("GROUPTICKET DELETE");
        assertError("ATTRACTION CREATE;A01;Dragon");
        assertError("ATTRACTION ENTER;A01");
        assertError("ATTRACTION EXIT;A01");
        assertError("ATTRACTION LIST");
        assertError("ATTRACTION STATUS");
        assertTrue(park.getUsers().isEmpty());
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void reportsUnknownCommand() {
        assertError("FOO;bar");
        assertError("user create;Ana");
    }

    @Test
    void deletesUser() {
        processor.process(CREATE_ANA);
        assertError("USER DELETE;ana@correo.com;Mala1!");
        assertOk("USER DELETE;ana@correo.com;Clave1!", "USER DELETED");
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void updatesUser() {
        processor.process(CREATE_ANA);
        assertOk("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "USER UPDATED");
        assertEquals("699999999", park.findUser("ana@correo.com").getPhone());
    }

    @Test
    void rejectsDniAndUnknownFields() {
        processor.process(CREATE_ANA);
        assertError("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertError("USER UPDATE;ana@correo.com;Clave1!;AGE;30");
        assertEquals("12345678Z", park.findUser("ana@correo.com").getDni());
    }

    @Test
    void systemCommands() {
        assertOk("SYSTEM DATE;2027-04-20", "SYSTEM DATE SET: 2027-04-20");
        assertError("SYSTEM DATE;2027-02-30");
        assertOk("SYSTEM PRICE;45.5", "SYSTEM PRICE SET: 45.50 EUR");
        assertError("SYSTEM PRICE;-1");
    }

    @Test
    void groupAndTicketCommands() {
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        assertOk("GROUP CREATE;G01;Familia Garcia", "GROUP CREATED");
        assertOk("GROUP ADD;G01;ana@correo.com", "GROUP USER ADDED");
        assertError("GROUP ADD;G01;ana@correo.com");
        assertOk("GROUP ADD;G01;pedro@correo.com", "GROUP USER ADDED");
        assertOk("GROUP REMOVE;G01;pedro@correo.com", "GROUP USER REMOVED");
        assertOk("TICKET CREATE;T001;ana@correo.com;2027-04-15", "TICKET CREATED: price 40.00 EUR");
        assertOk("TICKET CREATE;T002;pedro@correo.com;2027-04-16", "TICKET CREATED: price 40.00 EUR");
        assertError("TICKET CREATE;T003;pedro@correo.com;2027-04-14");
        assertOk("TICKET DELETE;T002", "TICKET DELETED");
        assertError("TICKET DELETE;T001");
        assertOk("GROUPTICKET CREATE;TG01;G01;2027-04-16", "GROUPTICKET CREATED: total 40.00 EUR");
        assertError("GROUP ADD;G01;pedro@correo.com");
        assertError("GROUP DELETE;G01");
        assertOk("GROUPTICKET DELETE;TG01", "GROUPTICKET DELETED");
        assertOk("GROUP DELETE;G01", "GROUP DELETED");
    }

    @Test
    void attractionCommands() {
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertOk("ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATED");
        assertError("ATTRACTION CREATE;A02;Torre;0");
        assertOk("ATTRACTION LIST;A01", "(no users)");
        assertOk("ATTRACTION ENTER;A01;TG01", "ATTRACTION ENTERED: 2 user(s)");
        assertOk("ATTRACTION EXIT;A01;ana@correo.com", "ATTRACTION EXITED");
        assertOk("ATTRACTION LIST;A01", "pedro@correo.com");

        output.reset();
        assertTrue(processor.process("ATTRACTION STATUS;A01"));
        String status = output.toString();
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Dragon Rojo"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Users inside: 1"));
        assertTrue(status.contains("Available places: 19"));
        assertTrue(status.contains("pedro@correo.com"));
        assertError("ATTRACTION STATUS;A99");
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String help = output.toString();
        for (CommandInfo command : new CommandCatalog().commands()) {
            assertTrue(help.contains(command.syntax()), command.syntax());
        }
        assertTrue(help.contains("ATTRACTION STATUS;<attractionIdentifier>"));
        assertTrue(help.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
    }

    @Test
    void delivery3Commands() {
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertOk("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL", "ATTRACTION CREATED");
        assertError("ATTRACTION CREATE;A05;X;15;VIP");
        assertOk("SPEEDPASS CREATE;SP02;T001;10", "SPEEDPASS CREATED: 10 pass(es), total 80.00 EUR");
        assertError("SPEEDPASS CREATE;SP03;T001;5");
        assertError("SPEEDPASS CREATE;SP01;T001");
        assertOk("SPECIALPASS CREATE;PE01;T001", "SPECIALPASS CREATED: price 40.00 EUR");
        assertError("ATTRACTION ENTER;A04;T001;SPEED");
        assertError("ATTRACTION ENTER;A04;T001;FAST");
        assertOk("ATTRACTION ENTER;A04;T001", "ATTRACTION ENTERED: 1 user(s); Special Pass consumed: PE01");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertOk("ATTRACTION ENTER;A01;T001;SPEED", "ATTRACTION ENTERED: 1 user(s); SpeedPass consumed: SP02:1");
        assertError("SPEEDPASS DELETE;SP02:2");
        assertError("SPECIALPASS DELETE;PE01");

        output.reset();
        assertTrue(processor.process("INVOICE CREATE;USER;ana@correo.com"));
        String invoice = output.toString();
        assertTrue(invoice.contains("INVOICE - USER"));
        assertTrue(invoice.contains("SpeedPass SP02:10"));
        assertTrue(invoice.contains("TOTAL: 160.00 EUR"));
        assertError("INVOICE CREATE;COMPANY;X");
        assertError("INVOICE CREATE;GROUP;G99");

        output.reset();
        assertTrue(processor.process("HELP EXTENDED"));
        assertTrue(output.toString().contains("INVOICE CREATE;GROUP;<groupIdentifier>"));
        assertTrue(output.toString().contains("Shows a group's invoice"));
    }

    @Test
    void runtimeErrorsAreContained() {
        HelpGenerator failing = new HelpGenerator(new CommandCatalog(), commands -> {
            throw new IllegalStateException("boom");
        });
        CommandProcessor fragile = new CommandProcessor(park, failing, failing, new InvoicePrinter(),
                new PrintStream(output, true));
        assertTrue(fragile.process("HELP"));
        assertTrue(lastLine().startsWith("ERROR: unexpected error"));
        assertTrue(fragile.process(CREATE_ANA));
        assertEquals("USER CREATED", lastLine());
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
        assertFalse(processor.process("  EXIT  "));
    }
}
