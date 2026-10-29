package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** All Delivery 1-3 commands working together. */
class Delivery3IntegrationTest {

    private static final List<String> DELIVERY3_MINIMUM = List.of(
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

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        for (int i = 1; i <= 5; i++) {
            park.registerUser(new User("u" + i + "@correo.com", "0000000" + i + "X"));
        }
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String script() {
        StringBuilder script = new StringBuilder("SYSTEM DATE;2027-04-15\nSYSTEM PRICE;40\nGROUP CREATE;G01;Seis\n"
                + "GROUP ADD;G01;ana@correo.com\n");
        for (int i = 1; i <= 5; i++) {
            script.append("GROUP ADD;G01;u").append(i).append("@correo.com\n");
        }
        return script.append("TICKET CREATE;T001;ana@correo.com;2027-04-15\n")
                .append("TICKET CREATE;T002;u1@correo.com;2027-04-20\n")
                .append("GROUPTICKET CREATE;TG01;G01;2027-04-15\n")
                .append("SPEEDPASS CREATE;SP01;T001;10\n")
                .append("SPECIALPASS CREATE;PE01;T001\n")
                .append("SPEEDPASS CREATE;SPC;TG01:12345678Z;1\n")
                .append("SPEEDPASS CREATE;SPX;T002;1\n")
                .append("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL\n")
                .append("ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL\n")
                .append("ATTRACTION CREATE;A03;Rafting Familiar;24\n")
                .append("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL\n")
                .append("ATTRACTION ENTER;A01;T001;SPEED\n")
                .append("ATTRACTION ENTER;A02;TG01\n")
                .append("ATTRACTION ENTER;A03;TG01\n")
                .append("ATTRACTION ENTER;A04;T001\n")
                .append("ATTRACTION EXIT;A01;ana@correo.com\n")
                .append("ATTRACTION STATUS;A03\n")
                .append("ATTRACTION LIST;A04\n")
                .append("SPEEDPASS DELETE;SPX:1\n")
                .append("TICKET DELETE;T002\n")
                .append("INVOICE CREATE;USER;ana@correo.com\n")
                .append("INVOICE CREATE;GROUP;G01\n")
                .append("HELP\nHELP EXTENDED\nEXIT\nGROUP CREATE;G99;Never\n").toString();
    }

    @Test
    void allDeliveriesWorkTogether() throws IOException {
        String out = run(script());
        assertTrue(out.contains("GROUP TICKET CREATED. TOTAL PRICE: 192.00 EUR"));
        assertTrue(out.contains("SPEEDPASS PURCHASED (10). TOTAL PRICE: 80.00 EUR"));
        assertTrue(out.contains("SPECIALPASS PURCHASED. PRICE: 40.00 EUR"));
        assertTrue(out.contains("SPEEDPASS SP01:1 CONSUMED"));
        assertTrue(out.contains("of type INDIVIDUAL does not accept ticket TG01"));
        assertTrue(out.contains("TYPE: GENERAL") && out.contains("USERS INSIDE: 6"));
        assertTrue(out.contains("SPECIALPASS PE01 CONSUMED"));
        assertTrue(out.contains("SPEEDPASS REFUNDED"));
        assertTrue(out.contains("TICKET DELETED"));
        assertTrue(out.contains("=== USER INVOICE ===") && out.contains("=== GROUP INVOICE ==="));
        assertTrue(out.contains("TOTAL: 192.00 EUR"));
        assertTrue(out.contains("Available commands (extended):"));
        assertTrue(park.findPass("PE01").isConsumed() && park.findPass("SP01:1").isConsumed());
        assertEquals(null, park.findGroup("G99"));
    }

    @Test
    void userInvoiceIncludesPassesOfAllHerTickets() throws IOException {
        run(script().replace("ATTRACTION ENTER;A01;T001;SPEED\n", ""));
        Invoice invoice = InvoiceController.getInstance().createUserInvoice("ana@correo.com");
        List<String> sections = invoice.getSections().stream().map(InvoiceSection::getTitle).toList();
        assertEquals(List.of("User", "Individual tickets", "SpeedPasses", "Special Passes"), sections);
        assertEquals(11, invoice.getSections().get(2).getLines().size());
        assertEquals(new BigDecimal("166.40"), invoice.getTotal());
        assertFalse(invoice.toText().contains("Ticket TG01:12345678Z"));
    }

    @Test
    void groupInvoiceExcludesMembersPasses() throws IOException {
        run(script());
        Invoice invoice = InvoiceController.getInstance().createGroupInvoice("G01");
        assertEquals(new BigDecimal("192.00"), invoice.getTotal());
        assertFalse(invoice.toText().contains("SPC:1"));
    }

    @Test
    void userWithoutPassesHasNoPassSections() {
        park.setCurrentDate("2027-04-15");
        park.createIndividualTicket("T005", "u5@correo.com", "2027-04-15");
        Invoice invoice = InvoiceController.getInstance().createUserInvoice("u5@correo.com");
        assertEquals(List.of("User", "Individual tickets"),
                invoice.getSections().stream().map(InvoiceSection::getTitle).toList());
    }

    @Test
    void helpShowsDelivery3MinimumSetWithDescriptions() throws IOException {
        String help = run("HELP\n");
        List<String> helpLines = help.lines().map(String::trim).toList();
        for (String syntax : DELIVERY3_MINIMUM) {
            assertTrue(helpLines.contains(syntax), "HELP missing " + syntax);
        }
        buffer.reset();
        List<String> extended = run("HELP EXTENDED\n").lines().toList();
        for (String syntax : DELIVERY3_MINIMUM) {
            int index = extended.indexOf("  " + syntax);
            assertTrue(index > 0, "HELP EXTENDED missing " + syntax);
            assertFalse(extended.get(index + 1).isBlank());
            assertTrue(extended.get(index + 1).startsWith("      "));
        }
    }
}
