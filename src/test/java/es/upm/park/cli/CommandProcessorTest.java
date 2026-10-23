package es.upm.park.cli;

import es.upm.park.service.Park;
import es.upm.park.service.ParkFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkFixture.openEmpty(dir);
        processor = new CommandProcessor(park);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createsUser() {
        CommandResult result = processor.process(CREATE_ANA);
        assertEquals("USER CREATED", result.output());
        assertFalse(result.exit());
    }

    @Test
    void commandNameIsCaseInsensitiveAndFieldsAreTrimmed() {
        assertEquals("USER CREATED",
                run("  user   create ; Ana ;Garcia Lopez;12345678Z; ana@correo.com ;Clave1!;612345678;1234567890123456"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;phone;699999999"));
    }

    @Test
    void updatesAndDeletesUser() {
        run(CREATE_ANA);
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER DELETED", run("USER DELETE;ana.nueva@correo.com;Clave1!"));
    }

    @Test
    void rejectedOperationsReportErrors() {
        run(CREATE_ANA);
        assertTrue(run(CREATE_ANA).startsWith("ERROR:"));
        assertTrue(run("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR:"));
        assertTrue(run("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
    }

    @Test
    void unknownCommandIsAnError() {
        CommandResult result = processor.process("FOO BAR");
        assertTrue(result.output().startsWith("ERROR: Unknown command"));
        assertFalse(result.exit());
    }

    @Test
    void wrongFieldCountShowsSyntax() {
        assertEquals("ERROR: Usage: USER DELETE;<Email>;<Password>", run("USER DELETE;ana@correo.com"));
        assertEquals("ERROR: Usage: GROUP ADD;<Identifier>;<Email>", run("GROUP ADD;G01"));
        assertTrue(run("HELP;extra").startsWith("ERROR:"));
    }

    @Test
    void systemDateAndPrice() {
        assertEquals("SYSTEM DATE SET TO 2027-04-15", run("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertEquals("SYSTEM PRICE SET TO 45.50", run("SYSTEM PRICE;45.5"));
        assertTrue(run("SYSTEM PRICE;-1").startsWith("ERROR:"));
        assertTrue(run("SYSTEM DATE;2027-02-30").startsWith("ERROR:"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
    }

    @Test
    void deliveryTwoFlow() {
        run("SYSTEM DATE;2027-04-15");
        run(CREATE_ANA);
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        run("USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444");
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("GROUP TICKET CREATED (3 tickets). TOTAL PRICE: 108.00 EUR",
                run("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        assertEquals("ATTRACTION ENTERED: 3 user(s)", run("ATTRACTION ENTER;A01;TG01"));
        assertEquals(String.join(System.lineSeparator(), "ana@correo.com", "pedro@correo.com", "laura@correo.com"),
                run("ATTRACTION LIST;A01"));
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Dragon Rojo"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Users inside: 3"));
        assertTrue(status.contains("Available places: 17"));
        assertTrue(status.contains("laura@correo.com"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T002;ana@correo.com;2027-04-20"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR:"));
        assertTrue(run("GROUP REMOVE;G01;laura@correo.com").startsWith("ERROR:"));
        assertTrue(run("GROUPTICKET DELETE;TG01").startsWith("ERROR:"));
        assertTrue(run("GROUP DELETE;G01").startsWith("ERROR:"));
    }

    @Test
    void emptyAttractionList() {
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        assertEquals("NO USERS IN ATTRACTION A02", run("ATTRACTION LIST;A02"));
        assertTrue(run("ATTRACTION STATUS;A02").contains("Users: (none)"));
        assertTrue(run("ATTRACTION LIST;A99").startsWith("ERROR:"));
    }

    @Test
    void helpListsEveryCommandFromTheSpecification() {
        String help = run("HELP");
        String[] expected = {
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
                "EXIT"};
        for (String syntax : expected) {
            assertTrue(help.contains(syntax), "missing " + syntax);
        }
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("exit").exit());
    }

    @Test
    void blankLineProducesNoOutput() {
        assertEquals("", run("   "));
    }

    @Test
    void normalizesCommandName() {
        assertEquals("USER CREATE", CommandProcessor.normalizeCommandName("  user \t create "));
    }
}
