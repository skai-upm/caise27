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

class PassCommandsTest {

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void passCommandsAndInvoices() throws IOException {
        String s = UUID.randomUUID().toString();
        String ana = "ana-" + s + "@correo.com";
        ParkController.getInstance().addUser(new User("A" + s, ana));
        String out = run("SYSTEM DATE;2027-04-01\nSYSTEM PRICE;40\n"
                + "GROUP CREATE;G" + s + ";Familia\n"
                + "GROUP ADD;G" + s + ";" + ana + "\n"
                + "GROUPTICKET CREATE;TG" + s + ";G" + s + ";2027-04-15\n"
                + "SPEEDPASS CREATE;SP" + s + ";TG" + s + ":A" + s + ";10\n"
                + "SPECIALPASS CREATE;PE" + s + ";TG" + s + ":A" + s + "\n"
                + "SPECIALPASS CREATE;PX" + s + ";TG" + s + ":A" + s + "\n"
                + "SPEEDPASS CREATE;SQ" + s + ";TG" + s + ":A" + s + ";3\n"
                + "SPEEDPASS DELETE;SP" + s + ":2\n"
                + "SPECIALPASS DELETE;SP" + s + ":3\n"
                + "INVOICE CREATE;USER;" + ana + "\n"
                + "INVOICE CREATE;GROUP;G" + s + "\n");
        assertTrue(out.contains("SPEEDPASS CREATED: 10 pass(es). TOTAL PRICE: 80.00"), out);
        assertTrue(out.contains("SPECIALPASS CREATED. PRICE: 40.00"), out);
        assertTrue(out.contains("SPEEDPASS DELETED"), out);
        assertEquals(3, out.split("ERROR", -1).length - 1, out);
        String userInvoice = out.substring(out.indexOf("INVOICE USER"), out.indexOf("INVOICE GROUP"));
        assertTrue(userInvoice.contains("[SpeedPasses]") && userInvoice.contains("[Special Pass]"), userInvoice);
        assertTrue(userInvoice.contains("TOTAL: 112.00"), userInvoice);
        String groupInvoice = out.substring(out.indexOf("INVOICE GROUP"));
        assertTrue(groupInvoice.contains("TOTAL: 40.00") && !groupInvoice.contains("Pass "), groupInvoice);
    }
}
