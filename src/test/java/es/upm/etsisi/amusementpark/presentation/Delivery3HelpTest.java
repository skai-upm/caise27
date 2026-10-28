package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class Delivery3HelpTest {

    private static final List<String> DELIVERY_3_MINIMUM = List.of(
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
            "EXIT");

    private static final List<String> EARLIER_DELIVERIES = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>");

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpShowsAllDeliveries() throws IOException {
        String help = run("HELP\n");
        for (String line : DELIVERY_3_MINIMUM) {
            assertTrue(help.contains(line + System.lineSeparator()), line);
        }
        for (String line : EARLIER_DELIVERIES) {
            assertTrue(help.contains(line + System.lineSeparator()), line);
        }
    }

    @Test
    void extendedHelpDescribesDelivery3Commands() throws IOException {
        String help = run("HELP EXTENDED\n");
        for (String line : DELIVERY_3_MINIMUM) {
            int at = help.indexOf(line + System.lineSeparator() + "    ");
            assertTrue(at >= 0, line);
        }
    }
}
