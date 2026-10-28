package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class InvoiceCommandTest {

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void renderedInvoicesAndRejections() throws IOException {
        String s = UUID.randomUUID().toString();
        String ana = "ana-" + s + "@correo.com";
        ParkController.getInstance().addUser(new User("D" + s, ana));
        String out = run("SYSTEM DATE;2027-04-01\nSYSTEM PRICE;40\n"
                + "GROUP CREATE;G" + s + ";Familia Garcia\n"
                + "GROUP ADD;G" + s + ";" + ana + "\n"
                + "GROUPTICKET CREATE;TG" + s + ";G" + s + ";2027-04-15\n"
                + "TICKET CREATE;T" + s + ";" + ana + ";2027-04-15\n"
                + "INVOICE CREATE;USER;" + ana + "\n"
                + "INVOICE CREATE;GROUP;G" + s + "\n"
                + "INVOICE CREATE;COMPANY;X\n"
                + "INVOICE CREATE;USER;nadie@correo.com\n"
                + "INVOICE CREATE;GROUP\n");
        String nl = System.lineSeparator();
        assertTrue(out.contains("INVOICE USER" + nl + "User: " + ana + nl + "DNI: D" + s + nl
                + "[Individual tickets]" + nl + "  Ticket T" + s + " (2027-04-15) ... 40.00" + nl
                + "TOTAL: 40.00"), out);
        assertTrue(out.contains("INVOICE GROUP" + nl + "Group: G" + s + nl + "Name: Familia Garcia" + nl
                + "[Group tickets]" + nl + "  Group ticket TG" + s + " (2027-04-15, 1 members) ... 40.00" + nl
                + "      - TG" + s + ":D" + s + " " + ana + " 40.00" + nl + "TOTAL: 40.00"), out);
        assertEquals(3, out.split("ERROR", -1).length - 1, out);
    }
}
