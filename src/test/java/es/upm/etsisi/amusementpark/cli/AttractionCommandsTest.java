package es.upm.etsisi.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.Attraction;
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

class AttractionCommandsTest {

    private final ParkController controller = ParkController.getInstance();
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
        id = "A-" + UUID.randomUUID();
    }

    @Test
    void createAndListEmpty() throws IOException {
        String out = run("ATTRACTION CREATE;" + id + ";Dragon Rojo;20\nATTRACTION LIST;" + id + "\n");
        assertEquals(0, errors(out), out);
        assertTrue(out.contains("ATTRACTION CREATED"));
        assertTrue(out.contains("NO USERS IN ATTRACTION " + id));
        Attraction attraction = controller.findAttraction(id).orElseThrow();
        assertEquals(20, attraction.getCapacity());
        assertEquals("Dragon Rojo", attraction.getName());
    }

    @Test
    void listShowsOccupantEmailsWithoutChangingState() throws IOException {
        run("ATTRACTION CREATE;" + id + ";Torre del Cielo;12\n");
        Attraction attraction = controller.findAttraction(id).orElseThrow();
        attraction.addOccupant(new User("12345678Z", "ana@correo.com"));
        attraction.addOccupant(new User("87654321X", "pedro@correo.com"));
        String out = run("ATTRACTION LIST;" + id + "\n");
        assertEquals("ana@correo.com" + System.lineSeparator() + "pedro@correo.com" + System.lineSeparator(), out);
        assertEquals(2, attraction.getOccupants().size());
    }

    @Test
    void rejections() throws IOException {
        String out = run("ATTRACTION CREATE;" + id + ";Dragon Rojo;20\n"
                + "ATTRACTION CREATE;" + id + ";Otra;5\n"
                + "ATTRACTION CREATE;" + id + "b;Noria;0\n"
                + "ATTRACTION CREATE;" + id + "c;Noria;abc\n"
                + "ATTRACTION CREATE;" + id + "d;Noria;-3\n"
                + "ATTRACTION CREATE;" + id + "e;Noria\n"
                + "ATTRACTION LIST;NO-SUCH\n");
        assertEquals(6, errors(out), out);
        assertEquals("Dragon Rojo", controller.findAttraction(id).orElseThrow().getName());
        assertTrue(controller.findAttraction(id + "b").isEmpty());
    }
}
