package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.service.ParkController;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.CommandInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end checks of the complete Delivery 3 feature set together with the
 * Delivery 1 and 2 commands.
 */
class Delivery3IntegrationTest {

    static final String SESSION = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave3$;600000000;1111222233334444",
            "SYSTEM DATE;2027-04-01",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T003;luis@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPECIALPASS CREATE;PE01;T001",
            "SPEEDPASS CREATE;SP05;TG01:12345678Z;1",
            "SPEEDPASS DELETE;SP01:10",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION ENTER;A03;TG01",
            "ATTRACTION ENTER;A02;T003",
            "SPEEDPASS DELETE;SP01:1",
            "ATTRACTION STATUS;A03",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "EXIT",
            "");

    private static final List<String> EXPECTED_BEFORE_INVOICES = List.of(
            "USER CREATED",
            "USER CREATED",
            "USER CREATED",
            "SYSTEM DATE UPDATED: 2027-04-01",
            "GROUP CREATED",
            "USER ADDED TO GROUP",
            "USER ADDED TO GROUP",
            "TICKET CREATED. PRICE: 40.00 EUR",
            "TICKET CREATED. PRICE: 40.00 EUR",
            "GROUP TICKET CREATED. TOTAL PRICE: 80.00 EUR",
            "SPEEDPASS CREATED: SP01:1..SP01:10. TOTAL PRICE: 80.00 EUR",
            "SPECIAL PASS CREATED. PRICE: 40.00 EUR",
            "SPEEDPASS CREATED: SP05:1. TOTAL PRICE: 8.00 EUR",
            "SPEEDPASS REFUNDED",
            "ATTRACTION CREATED",
            "ATTRACTION CREATED",
            "ATTRACTION CREATED",
            "ATTRACTION CREATED",
            "SYSTEM DATE UPDATED: 2027-04-15",
            "ERROR",
            "ERROR",
            "ERROR",
            "ENTERED ATTRACTION A01: ana@correo.com",
            "SPEEDPASS CONSUMED: SP01:1",
            "ENTERED ATTRACTION A04: ana@correo.com",
            "SPECIAL PASS CONSUMED: PE01",
            "ENTERED ATTRACTION A03: ana@correo.com, pedro@correo.com",
            "ENTERED ATTRACTION A02: luis@correo.com",
            "ERROR",
            "ATTRACTION A03 - Rafting Familiar",
            "Capacity: 24",
            "Users inside: 2",
            "Available places: 22",
            "Inside: ana@correo.com, pedro@correo.com",
            "INVOICE - USER");

    @TempDir
    Path tempDir;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), tempDir.resolve("data"), tempDir.resolve("tmp"));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void assertSession(String output) {
        List<String> lines = List.of(output.split(System.lineSeparator()));
        for (int i = 0; i < EXPECTED_BEFORE_INVOICES.size(); i++) {
            String expected = EXPECTED_BEFORE_INVOICES.get(i);
            if (expected.equals("ERROR")) {
                assertTrue(lines.get(i).startsWith("ERROR"), lines.get(i));
            } else {
                assertEquals(expected, lines.get(i));
            }
        }
        assertTrue(output.contains("Individual tickets:" + System.lineSeparator() + "  T001 (2027-04-15): 40.00 EUR"));
        assertTrue(output.contains("  SP05:1 (ticket TG01:12345678Z): 8.00 EUR"));
        assertTrue(output.contains("  SP01:9 (ticket T001): 8.00 EUR"));
        assertFalse(output.contains("SP01:10 (ticket"));
        assertTrue(output.contains("Special Passes:" + System.lineSeparator() + "  PE01 (ticket T001): 40.00 EUR"));
        assertTrue(output.contains("TOTAL: 160.00 EUR"));
        assertTrue(output.contains("INVOICE - GROUP" + System.lineSeparator() + "Group: G01 - Familia Garcia"));
        assertTrue(output.contains("TOTAL: 80.00 EUR"));
        assertEquals("Exiting application.", lines.get(lines.size() - 1));
    }

    @Test
    void completeSessionFromFileKeepsConsumedPassesAfterRestart() throws IOException {
        Path input = tempDir.resolve("input_fase3.txt");
        Files.writeString(input, SESSION, StandardCharsets.UTF_8);
        assertSession(run("", input.toString()));

        String second = run("SYSTEM DATE;2027-04-15\nATTRACTION ENTER;A04;T001\nSPECIALPASS CREATE;PE02;T001\nEXIT\n");
        String[] lines = second.split(System.lineSeparator());
        assertTrue(lines[1].startsWith("ERROR"), second);
        assertTrue(lines[2].startsWith("ERROR"), second);
        assertTrue(ParkController.getInstance().getPasses().findById("SP01:1").orElseThrow().isConsumed());
        assertTrue(ParkController.getInstance().getPasses().findById("PE01").orElseThrow().isConsumed());
        assertFalse(ParkController.getInstance().getPasses().findById("SP01:2").orElseThrow().isConsumed());
    }

    @Test
    void sameSessionWithFlyGivesSameOutputAndLeavesNoData() throws IOException {
        Path input = tempDir.resolve("input_fase3.txt");
        Files.writeString(input, SESSION, StandardCharsets.UTF_8);
        String fly = run("", "--fly", input.toString());
        assertSession(fly);
        assertFalse(Files.exists(tempDir.resolve("tmp")));
        assertFalse(Files.exists(tempDir.resolve("data")));
        assertEquals(fly, run("", input.toString()));
    }

    @Test
    void helpShowsTheCommandsOfAllDeliveries() {
        String help = run("HELP\nEXIT\n");
        for (String syntax : List.of(
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "SPEEDPASS DELETE;<speedPassIdentifier>",
                "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "SPECIALPASS DELETE;<specialPassIdentifier>",
                "INVOICE CREATE;USER;<email>",
                "INVOICE CREATE;GROUP;<groupIdentifier>",
                "HELP",
                "HELP EXTENDED",
                "EXIT",
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                "SYSTEM PRICE;<Price>")) {
            assertTrue(help.contains("  " + syntax + System.lineSeparator()), syntax);
        }
        String extended = run("HELP EXTENDED\nEXIT\n");
        for (CommandInfo command : CommandCatalog.commands()) {
            assertTrue(extended.contains(command.description()), command.syntax());
        }
    }
}
