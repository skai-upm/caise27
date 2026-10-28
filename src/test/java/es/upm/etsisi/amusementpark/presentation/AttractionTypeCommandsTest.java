package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AttractionTypeCommandsTest {

    private final ParkController controller = ParkController.getInstance();

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void typedCreationAndRestrictedEntry() throws IOException {
        String s = UUID.randomUUID().toString();
        String ana = "ana-" + s + "@correo.com";
        controller.addUser(new User("A" + s, ana));
        String out = run("SYSTEM DATE;2027-04-15\nSYSTEM PRICE;40\n"
                + "ATTRACTION CREATE;A1" + s + ";Dragon Rojo;20\n"
                + "ATTRACTION CREATE;A2" + s + ";Montana Rusa Individual;12;INDIVIDUAL\n"
                + "ATTRACTION CREATE;A3" + s + ";Rafting Familiar;24;GROUP\n"
                + "ATTRACTION CREATE;A4" + s + ";Pasaje del Terror;15;SPECIAL\n"
                + "ATTRACTION CREATE;A5" + s + ";Noria;5;VIP\n"
                + "GROUP CREATE;G" + s + ";Familia\nGROUP ADD;G" + s + ";" + ana + "\n"
                + "GROUPTICKET CREATE;TG" + s + ";G" + s + ";2027-04-15\n"
                + "TICKET CREATE;T" + s + ";" + ana + ";2027-04-15\n"
                + "ATTRACTION ENTER;A2" + s + ";TG" + s + "\n"
                + "ATTRACTION ENTER;A3" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;A4" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;A1" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;A2" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;A3" + s + ";TG" + s + "\n"
                + "SPECIALPASS CREATE;PE" + s + ";T" + s + "\n"
                + "ATTRACTION ENTER;A4" + s + ";T" + s + "\n"
                + "ATTRACTION STATUS;A4" + s + "\n");
        assertEquals(4, out.split("ERROR", -1).length - 1, out);
        assertEquals(4, out.split("ATTRACTION ENTERED", -1).length - 1, out);
        assertEquals(AttractionType.GENERAL, controller.findAttraction("A1" + s).orElseThrow().getType());
        assertEquals(AttractionType.SPECIAL, controller.findAttraction("A4" + s).orElseThrow().getType());
        assertTrue(out.contains("TYPE: SPECIAL"), out);
        assertTrue(controller.findAttraction("A5" + s).isEmpty());
    }
}
