package es.upm.etsisi.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketCommandsTest {

    private final ParkController controller = ParkController.getInstance();
    private String ana;
    private String id;

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int errors(String out) {
        int count = 0;
        for (int i = out.indexOf("ERROR"); i >= 0; i = out.indexOf("ERROR", i + 1)) {
            count++;
        }
        return count;
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        ana = "ana-" + suffix + "@correo.com";
        id = "T-" + suffix;
        controller.addUser(new User(ana));
        controller.setCurrentDate("2027-04-01");
        controller.setCurrentPrice("40.00");
    }

    @Test
    void createShowsPriceAndDeleteFutureTicket() throws IOException {
        String out = run("TICKET CREATE;" + id + ";" + ana + ";2027-04-15\n"
                + "SYSTEM PRICE;45.50\n"
                + "TICKET CREATE;" + id + "b;" + ana + ";2027-04-01\n"
                + "TICKET DELETE;" + id + "\n");
        assertEquals(0, errors(out), out);
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00"));
        assertTrue(out.contains("TICKET CREATED. PRICE: 45.50"));
        assertTrue(out.contains("TICKET DELETED"));
        assertFalse(controller.isTicketIdentifierInUse(id));
    }

    @Test
    void rejections() throws IOException {
        String out = run("TICKET CREATE;" + id + ";" + ana + ";2027-04-01\n"
                + "TICKET CREATE;" + id + ";" + ana + ";2027-04-02\n"
                + "TICKET CREATE;" + id + "x;nadie@correo.com;2027-04-02\n"
                + "TICKET CREATE;" + id + "y;" + ana + ";2027-03-31\n"
                + "TICKET CREATE;" + id + "z;" + ana + "\n"
                + "TICKET DELETE;" + id + "\n"
                + "TICKET DELETE;NO-SUCH-TICKET\n");
        assertEquals(6, errors(out), out);
        assertTrue(controller.isTicketIdentifierInUse(id));
    }
}
