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

class AttractionEntryCommandTest {

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void speedAndSpecialEntries() throws IOException {
        String s = UUID.randomUUID().toString();
        String ana = "ana-" + s + "@correo.com";
        ParkController.getInstance().addUser(new User("A" + s, ana));
        String nl = System.lineSeparator();
        String out = run("SYSTEM DATE;2027-04-15\n"
                + "ATTRACTION CREATE;G" + s + ";Dragon Rojo;20;GENERAL\n"
                + "ATTRACTION CREATE;S" + s + ";Pasaje del Terror;15;SPECIAL\n"
                + "TICKET CREATE;T" + s + ";" + ana + ";2027-04-15\n"
                + "ATTRACTION ENTER;G" + s + ";T" + s + ";SPEED\n"
                + "SPEEDPASS CREATE;SP" + s + ";T" + s + ";1\n"
                + "ATTRACTION ENTER;G" + s + ";T" + s + ";FAST\n"
                + "ATTRACTION ENTER;G" + s + ";T" + s + ";SPEED\n"
                + "SPECIALPASS CREATE;PE" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;S" + s + ";T" + s + ";SPEED\n"
                + "ATTRACTION ENTER;S" + s + ";T" + s + "\n"
                + "ATTRACTION EXIT;S" + s + ";" + ana + "\n"
                + "ATTRACTION ENTER;S" + s + ";T" + s + "\n");
        assertTrue(out.contains("ATTRACTION ENTERED: 1 user(s) in G" + s + nl + "SPEEDPASS CONSUMED: SP" + s + ":1"), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 1 user(s) in S" + s + nl + "SPECIALPASS CONSUMED: PE" + s), out);
        assertEquals(4, out.split("ERROR", -1).length - 1, out);
    }
}
