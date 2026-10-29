package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs a session combining the commands of the three deliveries.
 */
class DeliveryThreeIntegrationTest {

    static final List<String> DELIVERY_THREE_SYNTAX = List.of(
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>", "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP", "HELP EXTENDED", "EXIT");

    static final String SESSION = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez Gil;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444",
            "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;ana@correo.com;2027-04-20",
            "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "ATTRACTION CREATE;A05;Clasica;10",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPEEDPASS CREATE;SP02;TG01:12345678Z;1",
            "SPEEDPASS CREATE;SP03;TG01:87654321X;1",
            "SPECIALPASS CREATE;PE01;T001",
            "SPEEDPASS CREATE;SP04;T002;1",
            "SPEEDPASS DELETE;SP04:1",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "SPEEDPASS CREATE;SP05;TG01:11111111H;1",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION STATUS;A03",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "USER DELETE;luis@correo.com;Luis3$",
            "TICKET DELETE;T002",
            "HELP",
            "HELP EXTENDED",
            "EXIT",
            "USER CREATE;Nadie;X;22222222J;nadie@correo.com;Nadie4%;6;1234123412341234");

    @TempDir
    Path dir;

    private String run(Path data, String stdin, String... args) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        assertEquals(0, App.execute(args, new BufferedReader(new StringReader(stdin)), out, out, data, dir.resolve("tmp")));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void combinedSessionFromFileAndRestart() throws Exception {
        Path input = dir.resolve("input_fase3.txt");
        Files.writeString(input, SESSION);
        Path data = dir.resolve("data");
        String out = run(data, "", input.toString());
        assertTrue(out.contains("GROUPTICKET CREATED. Total price: 108.00 EUR"));
        assertTrue(out.contains("SPEEDPASS CREATED. Total price: 80.00 EUR"));
        assertTrue(out.contains("SPEEDPASS CREATED. Total price: 7.20 EUR"));
        assertTrue(out.contains("SPECIALPASS CREATED. Price: 40.00 EUR"));
        assertTrue(out.contains("SPEEDPASS DELETED"));
        assertTrue(out.contains("ERROR: Ticket TG01:11111111H has no unconsumed SpeedPass"));
        assertTrue(out.contains("SPEEDPASS CONSUMED: SP05:1 (ticket TG01:11111111H)"));
        assertTrue(out.contains("ERROR: INDIVIDUAL attraction A02 does not accept this ticket"));
        assertTrue(out.contains("SPEEDPASS CONSUMED: SP01:1 (ticket T001)"));
        assertTrue(out.contains("ERROR: SPECIAL attractions do not accept SpeedPasses"));
        assertTrue(out.contains("SPECIALPASS CONSUMED: PE01 (ticket T001)"));
        assertTrue(out.contains("Type: GROUP"));
        assertTrue(out.contains("Occupants: 3"));
        assertTrue(out.contains("INVOICE - USER"));
        assertTrue(out.contains("TOTAL: 207.20 EUR"), out);
        assertTrue(out.contains("TOTAL: 108.00 EUR"));
        assertTrue(out.contains("ERROR: User has active tickets"));
        assertTrue(out.contains("TICKET DELETED"));
        for (String syntax : DELIVERY_THREE_SYNTAX) {
            assertTrue(out.contains(syntax), syntax);
        }
        DeliveryTwoIntegrationTest.HELP_SYNTAX.forEach(s -> assertTrue(out.contains(s), s));
        assertTrue(out.contains("Available commands (extended):"));
        List<String> lines = List.of(out.split("\\R"));
        assertEquals("Goodbye", lines.get(lines.size() - 1));
        assertFalse(Files.readString(data.resolve("users.txt")).contains("Nueva2#"));

        String restarted = run(data, String.join("\n", "SYSTEM DATE;2027-04-15",
                "ATTRACTION LIST;A01", "ATTRACTION ENTER;A04;T001", "USER UPDATE;ana@correo.com;Nueva2#;PHONE;6",
                "SPECIALPASS CREATE;PE02;T001", "INVOICE CREATE;USER;ana@correo.com"));
        assertTrue(restarted.contains("NO USERS IN ATTRACTION A01"));
        assertTrue(restarted.contains("ERROR: Ticket T001 has no unconsumed Special Pass"));
        assertTrue(restarted.contains("USER UPDATED"));
        assertTrue(restarted.contains("ERROR: Ticket already has a Special Pass"));
        assertTrue(restarted.contains("TOTAL: 167.20 EUR"), restarted);
    }

    @Test
    void combinedSessionInFlyModeLeavesNoData() throws Exception {
        Path input = dir.resolve("input_fase3.txt");
        Files.writeString(input, SESSION);
        Path data = dir.resolve("data");
        String out = run(data, "", "--fly", input.toString());
        assertTrue(out.contains("SPECIALPASS CONSUMED: PE01 (ticket T001)"));
        assertTrue(Files.notExists(data.resolve("users.txt")));
        assertTrue(Files.notExists(data.resolve("passes.txt")));
        try (var left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void extendedHelpDescribesEveryCommand() {
        String extended = new HelpGenerator(CommandCatalog.all(), new ExtendedHelpFormat()).generate();
        for (CommandDescriptor c : CommandCatalog.all()) {
            assertTrue(extended.contains(c.syntax() + System.lineSeparator() + "    " + c.description()), c.syntax());
            assertFalse(c.description().isBlank());
        }
    }
}
