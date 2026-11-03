package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    private ParkController park;
    private ByteArrayOutputStream output;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws ParkException {
        park = initialize(dir.resolve("main"));
        output = new ByteArrayOutputStream();
        processor = new CommandProcessor(park, new PrintStream(output, true));
    }

    private static ParkController initialize(Path data) throws ParkException {
        data.toFile().mkdirs();
        ParkController controller = ParkController.getInstance();
        controller.initialize(new ParkRepositories(data, new PrintStream(new ByteArrayOutputStream())),
                ParkControllerTest.TODAY);
        return controller;
    }

    private String run(String... lines) throws Exception {
        processor.run(new BufferedReader(new StringReader(String.join("\n", lines))));
        return output.toString();
    }

    private static int count(String text, String fragment) {
        return text.split(fragment, -1).length - 1;
    }

    @Test
    void createsUser() throws Exception {
        assertTrue(run(CREATE_ANA).contains("USER CREATED"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void reportsMalformedAndUnknownCommandsAndContinues() throws Exception {
        String result = run("USER CREATE;Ana", "FOO BAR", "USER DELETE;x", "USER UPDATE;a;b",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234", "GROUP CREATE;G01",
                "TICKET CREATE;T1;x", "ATTRACTION ENTER;A01", "SYSTEM DATE", "HELP;x", "EXIT;now", CREATE_ANA);
        assertEquals(11, count(result, "ERROR:"));
        assertTrue(result.contains("USER CREATED"));
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void deletesAndUpdatesUsers() throws Exception {
        String result = run(CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER DELETE;pedro@correo.com;Otra2#",
                "USER DELETE;ana@correo.com;Wrong1!");
        assertTrue(result.contains("USER UPDATED"));
        assertTrue(result.contains("USER DELETED"));
        assertTrue(result.contains("ERROR: incorrect password"));
        assertEquals("699999999", park.getUsers().get(0).getPhone());
    }

    @Test
    void ignoresBlankLines() throws Exception {
        assertEquals("USER CREATED" + System.lineSeparator(), run("", "   ", CREATE_ANA, ""));
    }

    @Test
    void helpListsAllCommands() throws Exception {
        String result = run("HELP");
        for (String syntax : List.of(
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
                "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT")) {
            assertTrue(result.contains(syntax), syntax);
        }
    }

    @Test
    void exitStopsProcessing() throws Exception {
        String result = run(CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertTrue(result.contains("BYE"));
        assertEquals(1, park.getUsers().size());
        assertFalse(processor.execute("EXIT"));
        assertTrue(processor.execute("HELP"));
    }

    @Test
    void systemCommands() throws Exception {
        String result = run("SYSTEM DATE;2027-05-01", "SYSTEM PRICE;45.50", "SYSTEM PRICE;-1", "SYSTEM DATE;mañana");
        assertTrue(result.contains("SYSTEM DATE SET TO 2027-05-01"));
        assertTrue(result.contains("SYSTEM PRICE SET TO 45.50 EUR"));
        assertEquals(2, count(result, "ERROR:"));
    }

    @Test
    void groupAndTicketCommandsShowPrices() throws Exception {
        String result = run(CREATE_ANA, CREATE_PEDRO,
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUP REMOVE;G01;pedro@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "SYSTEM PRICE;45.50",
                "TICKET CREATE;T002;ana@correo.com;2027-04-20",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20", "GROUPTICKET DELETE;TG01", "TICKET DELETE;T002",
                "TICKET DELETE;T001", "GROUP DELETE;G01");
        assertTrue(result.contains("TICKET CREATED. PRICE: 40.00 EUR"));
        assertTrue(result.contains("TICKET CREATED. PRICE: 45.50 EUR"));
        assertTrue(result.contains("GROUP TICKET CREATED. TOTAL PRICE: 91.00 EUR"));
        assertTrue(result.contains("GROUP TICKET DELETED"));
        assertTrue(result.contains("TICKET DELETED"));
        assertTrue(result.contains("GROUP DELETED"));
        assertEquals(2, count(result, "ERROR:"));
    }

    @Test
    void attractionCommands() throws Exception {
        String result = run(CREATE_ANA, CREATE_PEDRO, "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION LIST;A01", "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION ENTER;A01;T001", "ATTRACTION LIST;A01", "ATTRACTION STATUS;A01",
                "ATTRACTION EXIT;A01;ana@correo.com", "ATTRACTION STATUS;A01", "ATTRACTION LIST;A99");
        assertTrue(result.contains("ATTRACTION CREATED"));
        assertEquals(1, count(result, "NO USERS IN ATTRACTION A01"));
        assertTrue(result.contains("ENTERED ATTRACTION: 1 USER(S)"));
        assertTrue(result.contains("ATTRACTION A01 - Dragon Rojo"));
        assertTrue(result.contains("Capacity: 20"));
        assertTrue(result.contains("Occupied: 1"));
        assertTrue(result.contains("Available: 19"));
        assertTrue(result.contains("Users: ana@correo.com"));
        assertTrue(result.contains("Users: none"));
        assertTrue(result.contains("EXITED ATTRACTION"));
        assertEquals(1, count(result, "ERROR:"));
    }

    @Test
    void fileAndReaderProduceSameResult() throws Exception {
        String[] sequence = {CREATE_ANA, "", CREATE_PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "USER DELETE;pedro@correo.com;Bad1!", "EXIT"};
        String interactive = run(sequence);

        ParkController filePark = initialize(dir.resolve("file"));
        ByteArrayOutputStream fileOutput = new ByteArrayOutputStream();
        Path commands = Files.write(dir.resolve("input.txt"), List.of(sequence));
        new CommandProcessor(filePark, new PrintStream(fileOutput, true)).runFile(commands);

        assertEquals(interactive, fileOutput.toString());
        for (String file : List.of("users.txt", "groups.txt")) {
            assertEquals(Files.readAllLines(dir.resolve("main").resolve(file)),
                    Files.readAllLines(dir.resolve("file").resolve(file)));
        }
    }
}
