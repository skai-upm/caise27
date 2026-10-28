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

class AttractionStatusCommandTest {

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

    @Test
    void statusAfterMixedEntriesAndExit() throws IOException {
        String s = UUID.randomUUID().toString();
        StringBuilder in = new StringBuilder("SYSTEM DATE;2027-04-15\nGROUP CREATE;G" + s + ";Familia\n");
        String[] emails = new String[4];
        for (int i = 0; i < 4; i++) {
            emails[i] = "u" + i + "-" + s + "@correo.com";
            ParkController.getInstance().addUser(new User("D" + i + s, emails[i]));
            if (i < 3) {
                in.append("GROUP ADD;G").append(s).append(';').append(emails[i]).append('\n');
            }
        }
        in.append("GROUPTICKET CREATE;TG").append(s).append(";G").append(s).append(";2027-04-15\n")
                .append("TICKET CREATE;T").append(s).append(';').append(emails[3]).append(";2027-04-15\n")
                .append("ATTRACTION CREATE;A").append(s).append(";Dragon Rojo;20\n")
                .append("ATTRACTION ENTER;A").append(s).append(";TG").append(s).append('\n')
                .append("ATTRACTION ENTER;A").append(s).append(";T").append(s).append('\n')
                .append("ATTRACTION EXIT;A").append(s).append(';').append(emails[1]).append('\n')
                .append("ATTRACTION EXIT;A").append(s).append(';').append(emails[1]).append('\n')
                .append("ATTRACTION STATUS;A").append(s).append('\n')
                .append("ATTRACTION STATUS;NOPE\n");
        String out = run(in.toString());
        assertEquals(2, errors(out), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 3 user(s)"), out);
        assertTrue(out.contains("ATTRACTION EXITED"), out);
        assertTrue(out.contains("ATTRACTION: A" + s), out);
        assertTrue(out.contains("NAME: Dragon Rojo"), out);
        assertTrue(out.contains("CAPACITY: 20"), out);
        assertTrue(out.contains("USERS INSIDE: 3"), out);
        assertTrue(out.contains("AVAILABLE PLACES: 17"), out);
        assertTrue(out.contains("OCCUPANTS: " + emails[0] + ", " + emails[2] + ", " + emails[3]), out);
    }
}
