package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delivery 3 end-to-end run mixing commands of the three deliveries, normally and with --fly.
 */
class Delivery3IntegrationTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String LATER = LocalDate.now().plusDays(5).toString();

    private static final String[] REQUIRED_SYNTAX = {
        "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
        "USER DELETE;<Email>;<Password>",
        "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
        "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
        "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>",
        "GROUP DELETE;<Identifier>", "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
        "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
        "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>", "ATTRACTION ENTER;<attraction>;<ticket>",
        "ATTRACTION ENTER;<attraction>;<ticket>;SPEED", "ATTRACTION EXIT;<attraction>;<email>",
        "ATTRACTION LIST;<attraction>", "ATTRACTION STATUS;<attractionIdentifier>",
        "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", "SPEEDPASS DELETE;<speedPassIdentifier>",
        "SPECIALPASS CREATE;<identifier>;<individualTicket>", "SPECIALPASS DELETE;<specialPassIdentifier>",
        "INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP", "HELP EXTENDED", "EXIT"};

    private static final String INPUT = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444",
            "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;" + TODAY,
            "TICKET CREATE;T001;ana@correo.com;" + TODAY,
            "TICKET CREATE;T002;pedro@correo.com;" + LATER,
            "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "ATTRACTION CREATE;A05;Clasica;5",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPEEDPASS CREATE;SPA;TG01:12345678Z;1",
            "SPEEDPASS CREATE;SPB;TG01:87654321X;1",
            "SPECIALPASS CREATE;PE01;T001",
            "SPEEDPASS CREATE;SP09;T002;1",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "SPEEDPASS CREATE;SPC;TG01:11111111H;1",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A02;T001;SPEED",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION STATUS;A03",
            "SPECIALPASS DELETE;PE01",
            "SPEEDPASS DELETE;SP09:1",
            "USER DELETE;pedro@correo.com;Otra2#",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "TICKET DELETE;T002",
            "EXIT",
            "USER CREATE;X;Y;00000000T;x@correo.com;Clave1!;1;1234567890123456") + "\n";

    @TempDir
    Path tmp;

    private String run(Path base, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = Main.run(args, new ByteArrayInputStream("HELP\nHELP EXTENDED\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()), base);
        assertEquals(0, code);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpCoversAllDeliveries() throws IOException {
        String out = run(Files.createDirectory(tmp.resolve("help")));
        int extended = out.indexOf("Available commands (extended):");
        String help = out.substring(0, extended);
        String extendedHelp = out.substring(extended);
        for (String syntax : REQUIRED_SYNTAX) {
            assertTrue(help.contains("\n" + syntax + "\n") || help.endsWith("\n" + syntax + "\n"), syntax);
            int at = extendedHelp.indexOf("\n" + syntax + "\n    ");
            assertTrue(at >= 0, "no description for " + syntax);
        }
    }

    @Test
    void phaseThreeFileRunsIdenticallyInNormalAndFlyModes() throws IOException {
        Path input = Files.writeString(tmp.resolve("input_fase3.txt"), INPUT);
        Path normal = Files.createDirectory(tmp.resolve("normal"));
        Path fly = Files.createDirectory(tmp.resolve("fly"));

        String normalOut = run(normal, input.toString());
        String flyOut = run(fly, "--fly", input.toString());
        assertEquals(normalOut, flyOut);

        for (String expected : new String[] {
                "GROUPTICKET CREATED - Total price: 108.00 EUR",
                "SPEEDPASS CREATED - Total price: 80.00 EUR",
                "SPEEDPASS CREATED - Total price: 7.20 EUR",
                "SPECIALPASS CREATED - Price: 40.00 EUR",
                "ERROR: Ticket TG01:11111111H has no available SpeedPass",
                "ENTERED A03: ana@correo.com pedro@correo.com luis@correo.com (consumed SpeedPass SPA:1)"
                        + " (consumed SpeedPass SPB:1) (consumed SpeedPass SPC:1)",
                "ERROR: Attraction A02 (INDIVIDUAL) does not accept this ticket type",
                "ENTERED A02: ana@correo.com (consumed SpeedPass SP01:1)",
                "ERROR: SpeedPasses are not accepted at special attractions",
                "ENTERED A04: ana@correo.com (consumed Special Pass PE01)",
                "Type: GROUP\nCapacity: 24\nUsers inside: 3",
                "ERROR: Consumed passes cannot be refunded",
                "SPEEDPASS DELETED",
                "ERROR: User has active tickets",
                "[SpeedPasses]",
                "[Special passes]\n  PE01 (ticket T001): 40.00 EUR",
                "INVOICE (GROUP)\nGroup: G01\nName: Familia Garcia\n[Group tickets]",
                "TICKET DELETED"}) {
            assertTrue(normalOut.contains(expected), expected + "\n---\n" + normalOut);
        }
        assertFalse(normalOut.contains("x@correo.com"));

        String passes = Files.readString(normal.resolve("passes.txt"));
        assertTrue(passes.contains("SPECIAL;PE01;T001;40.00;true"), passes);
        assertTrue(passes.contains("SPEED;SPA:1;TG01:12345678Z;7.20;true"), passes);
        assertFalse(passes.contains("SP09:1"), passes);
        assertFalse(Files.readString(normal.resolve("users.txt")).contains("Nueva2#"));
        assertFalse(Files.exists(fly.resolve("tmp")));
        assertFalse(Files.exists(fly.resolve("passes.txt")));
    }
}
