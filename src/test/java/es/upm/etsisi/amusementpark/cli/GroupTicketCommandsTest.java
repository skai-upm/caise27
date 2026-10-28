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

class GroupTicketCommandsTest {

    private final ParkController controller = ParkController.getInstance();
    private String suffix;

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
        suffix = UUID.randomUUID().toString();
        controller.setCurrentDate("2027-04-01");
        controller.setCurrentPrice("40");
        StringBuilder commands = new StringBuilder("GROUP CREATE;G" + suffix + ";Grupo\n");
        for (int i = 0; i < 6; i++) {
            String email = "u" + i + "-" + suffix + "@correo.com";
            controller.addUser(new User("D" + i + suffix, email));
            commands.append("GROUP ADD;G").append(suffix).append(';').append(email).append('\n');
        }
        try {
            assertEquals(0, errors(run(commands.toString())));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void createShowsDiscountedTotalAndDelete() throws IOException {
        String out = run("GROUPTICKET CREATE;TG" + suffix + ";G" + suffix + ";2027-04-15\n"
                + "GROUP ADD;G" + suffix + ";u0-" + suffix + "@correo.com\n"
                + "GROUP DELETE;G" + suffix + "\n"
                + "GROUPTICKET DELETE;TG" + suffix + "\n"
                + "GROUP DELETE;G" + suffix + "\n");
        assertTrue(out.contains("GROUPTICKET CREATED. TOTAL PRICE: 192.00"), out);
        assertEquals(2, errors(out), out);
        assertTrue(out.contains("GROUPTICKET DELETED"));
        assertTrue(out.contains("GROUP DELETED"));
        assertFalse(controller.isTicketIdentifierInUse("TG" + suffix));
    }

    @Test
    void rejections() throws IOException {
        String out = run("GROUP CREATE;E" + suffix + ";Vacio\n"
                + "GROUPTICKET CREATE;TE" + suffix + ";E" + suffix + ";2027-04-15\n"
                + "GROUPTICKET CREATE;TG" + suffix + ";G" + suffix + ";2027-03-31\n"
                + "GROUPTICKET CREATE;TG" + suffix + ";NOPE;2027-04-15\n"
                + "GROUPTICKET CREATE;TG" + suffix + ";G" + suffix + "\n"
                + "GROUPTICKET DELETE;NOPE\n"
                + "GROUP DELETE;NOPE\n");
        assertEquals(6, errors(out), out);
    }
}
