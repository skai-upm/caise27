package es.upm.etsisi.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.Test;

class AttractionEnterCommandTest {

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
    void enterListAndRejections() throws IOException {
        String s = UUID.randomUUID().toString();
        String ana = "ana-" + s + "@correo.com";
        String pedro = "pedro-" + s + "@correo.com";
        ParkController.getInstance().addUser(new User("A" + s, ana));
        ParkController.getInstance().addUser(new User("P" + s, pedro));
        String out = run("SYSTEM DATE;2027-04-15\n"
                + "ATTRACTION CREATE;A" + s + ";Dragon Rojo;1\n"
                + "TICKET CREATE;T1" + s + ";" + ana + ";2027-04-15\n"
                + "TICKET CREATE;T2" + s + ";" + pedro + ";2027-04-15\n"
                + "TICKET CREATE;T3" + s + ";" + pedro + ";2027-04-16\n"
                + "ATTRACTION ENTER;A" + s + ";T3" + s + "\n"
                + "ATTRACTION ENTER;A" + s + ";T1" + s + "\n"
                + "ATTRACTION ENTER;A" + s + ";T2" + s + "\n"
                + "ATTRACTION ENTER;A" + s + ";NOPE\n"
                + "ATTRACTION ENTER;NOPE;T1" + s + "\n"
                + "SYSTEM DATE;2027-04-16\n"
                + "ATTRACTION LIST;A" + s + "\n");
        assertTrue(out.contains("ATTRACTION ENTERED: 1 user(s) in A" + s), out);
        assertEquals(4, errors(out), out);
        assertTrue(out.endsWith(ana + System.lineSeparator()), out);
    }
}
