package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.ParkApplication;
import es.upm.etsisi.park.business.ParkTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CommandProcessorTest extends ParkTestSupport {

    public static final String CREATE_ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String CREATE_PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String NL = System.lineSeparator();

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = ParkApplication.commandProcessor(services);
    }

    private String runAll(String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    private String ok(String command) {
        String result = processor.execute(command);
        assertFalse(result.startsWith("ERROR"), command + " -> " + result);
        return result;
    }

    private void err(String command) {
        String result = processor.execute(command);
        assertTrue(result.startsWith("ERROR:"), command + " -> " + result);
    }

    // ---------------------------------------------------------------- Delivery 1

    @Test
    void createCommand() {
        assertEquals("USER CREATED", processor.execute(CREATE_ANA));
        assertEquals("Garcia Lopez", park.findUser("ana@correo.com").getSurnames());
        err(CREATE_ANA);
        assertEquals(1, park.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER CREATE;Ana;;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER CREATE",
        "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"
    })
    void malformedOrInvalidCreateIsRejected(String line) {
        err(line);
        assertTrue(park.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO BAR;1;2", "user create;Ana", "USER", "USER LIST", ";;;", "HELP;extra", "EXIT;now",
        "SYSTEM DATE", "SYSTEM PRICE;1;2", "GROUP CREATE;G01", "TICKET CREATE;T1;a@b.com", "ATTRACTION LIST",
        "GROUPTICKET DELETE", "ATTRACTION ENTER;A01"})
    void unknownOrMalformedCommandsAreErrors(String line) {
        err(line);
        assertFalse(processor.isExitRequested());
    }

    @Test
    void deleteAndUpdateCommands() {
        ok(CREATE_ANA);
        err("USER DELETE;ana@correo.com;Wrong1!");
        err("USER DELETE;ana@correo.com");
        assertEquals("USER UPDATED", processor.execute("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        err("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        err("USER UPDATE;ana@correo.com;Clave1!;PHONE");
        assertEquals("USER DELETED", processor.execute("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(park.getUsers().isEmpty());
    }

    @Test
    void helpListsAllCommandSyntax() {
        String help = processor.execute("HELP");
        for (String syntax : new String[] {
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
            assertTrue(help.contains(syntax), "HELP must contain " + syntax);
        }
    }

    @Test
    void runIgnoresBlankLinesAndKeepsOrder() throws IOException {
        String out = runAll(CREATE_ANA + "\n\n   \n" + CREATE_PEDRO + "\nUSER DELETE;pedro@correo.com;Otra2#\n");
        assertEquals(String.join(NL, "USER CREATED", "USER CREATED", "USER DELETED", ""), out);
    }

    @Test
    void exitStopsLaterCommands() throws IOException {
        String out = runAll(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertEquals(String.join(NL, "USER CREATED", "BYE", ""), out);
        assertTrue(processor.isExitRequested());
        assertNull(park.findUser("pedro@correo.com"));
    }

    @Test
    void errorsDoNotStopProcessing() throws IOException {
        String out = runAll("NOPE\n" + CREATE_ANA + "\n");
        assertTrue(out.startsWith("ERROR:"));
        assertTrue(out.contains("USER CREATED"));
    }

    // ---------------------------------------------------------------- Delivery 2

    @Test
    void systemCommands() {
        assertEquals("SYSTEM DATE SET TO 2027-05-01", processor.execute("SYSTEM DATE;2027-05-01"));
        assertEquals("SYSTEM PRICE SET TO 45.50 EUR", processor.execute("SYSTEM PRICE;45.50"));
        err("SYSTEM DATE;2027-13-01");
        err("SYSTEM PRICE;-1");
    }

    @Test
    void groupAndTicketCommands() {
        ok(CREATE_ANA);
        ok(CREATE_PEDRO);
        assertEquals("GROUP CREATED", processor.execute("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("GROUP USER ADDED", processor.execute("GROUP ADD;G01;ana@correo.com"));
        ok("GROUP ADD;G01;pedro@correo.com");
        err("GROUP ADD;G01;pedro@correo.com");
        assertEquals("TICKET CREATED T001 PRICE 40.00 EUR", processor.execute("TICKET CREATE;T001;ana@correo.com;" + TODAY_S));
        err("TICKET CREATE;T002;ana@correo.com;" + YESTERDAY_S);
        assertEquals("GROUPTICKET CREATED TG01 MEMBERS 2 DISCOUNT 0% TOTAL 80.00 EUR",
                processor.execute("GROUPTICKET CREATE;TG01;G01;" + TOMORROW_S));
        err("GROUP REMOVE;G01;ana@correo.com");
        err("TICKET DELETE;T001");
        err("TICKET DELETE;TG01:12345678Z");
        assertEquals("GROUPTICKET DELETED", processor.execute("GROUPTICKET DELETE;TG01"));
        assertEquals("GROUP USER REMOVED", processor.execute("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals("GROUP DELETED", processor.execute("GROUP DELETE;G01"));
        ok("TICKET CREATE;T009;pedro@correo.com;" + TOMORROW_S);
        assertEquals("TICKET DELETED", processor.execute("TICKET DELETE;T009"));
    }

    @Test
    void attractionCommands() {
        ok(CREATE_ANA);
        ok(CREATE_PEDRO);
        assertEquals("ATTRACTION CREATED", processor.execute("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        err("ATTRACTION CREATE;A02;Torre;0");
        assertEquals("(empty)", processor.execute("ATTRACTION LIST;A01"));
        ok("TICKET CREATE;T001;ana@correo.com;" + TODAY_S);
        ok("TICKET CREATE;T002;pedro@correo.com;" + TODAY_S);
        assertEquals("ATTRACTION ENTERED 1 USER(S)", processor.execute("ATTRACTION ENTER;A01;T001"));
        ok("ATTRACTION ENTER;A01;T002");
        assertEquals("ana@correo.com" + NL + "pedro@correo.com", processor.execute("ATTRACTION LIST;A01"));
        assertEquals("ana@correo.com" + NL + "pedro@correo.com", processor.execute("ATTRACTION LIST;A01"), "read-only");
        assertEquals("ATTRACTION EXITED", processor.execute("ATTRACTION EXIT;A01;ana@correo.com"));
        err("ATTRACTION EXIT;A01;ana@correo.com");
        assertEquals(String.join(NL,
                "ATTRACTION A01 - Dragon Rojo",
                "Type: GENERAL",
                "Capacity: 20",
                "Inside: 1",
                "Available: 19",
                "Users: pedro@correo.com"), processor.execute("ATTRACTION STATUS;A01"));
        err("ATTRACTION STATUS;A99");
        err("ATTRACTION LIST;A99");
    }
}
