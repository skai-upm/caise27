package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkAttractionsTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        park.reset();
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createSourceExamples() throws IOException {
        String out = run("ATTRACTION CREATE;A01;Dragon Rojo;20\nATTRACTION CREATE;A02;Torre del Cielo;12\n");
        assertEquals(2, out.split("ATTRACTION CREATED", -1).length - 1);
        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
        assertEquals(20, park.findAttraction("A01").getCapacity());
        assertEquals(12, park.findAttraction("A02").getCapacity());
    }

    @Test
    void invalidCreationsRejected() throws IOException {
        String out = run("ATTRACTION CREATE;A01;Dragon Rojo;20\n"
                + "ATTRACTION CREATE;A01;Otra;5\n"
                + "ATTRACTION CREATE;A03;X;0\n"
                + "ATTRACTION CREATE;A03;X;-1\n"
                + "ATTRACTION CREATE;A03;X;abc\n"
                + "ATTRACTION CREATE;A03; ;5\n"
                + "ATTRACTION CREATE;A03;X\n"
                + "ATTRACTION CREATE;A03;X;99999999999\n");
        assertTrue(out.contains("Error: attraction already exists: A01"));
        assertEquals(3 + 1, out.split("capacity must be a whole number", -1).length - 1);
        assertTrue(out.contains("Error: attraction name must not be empty"));
        assertTrue(out.contains("Error: invalid arguments for ATTRACTION CREATE"));
        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
        assertNull(park.findAttraction("A03"));
    }

    @Test
    void listOccupantsIsReadOnly() throws IOException {
        Attraction attraction = park.createAttraction("A01", "Dragon Rojo", "20");
        User ana = new User("ana@correo.com", "12345678Z");
        User pedro = new User("pedro@correo.com", "87654321X");
        attraction.addOccupant(ana);
        attraction.addOccupant(pedro);
        String out = run("ATTRACTION LIST;A01\n");
        assertTrue(out.contains("ana@correo.com"));
        assertTrue(out.contains("pedro@correo.com"));
        assertEquals(List.of(ana, pedro), attraction.getOccupants());
    }

    @Test
    void emptyAndUnknownAttraction() throws IOException {
        park.createAttraction("A02", "Torre del Cielo", "12");
        String out = run("ATTRACTION LIST;A02\nATTRACTION LIST;A99\n");
        assertTrue(out.contains("NO USERS IN ATTRACTION A02"));
        assertTrue(out.contains("Error: attraction not found: A99"));
        assertFalse(out.contains("could not process"));
    }

    @Test
    void helpListsAttractionCommands() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>"));
        assertTrue(out.contains("ATTRACTION LIST;<attraction>"));
    }
}
