package es.upm.amusementpark.business;

import es.upm.amusementpark.presentation.help.HelpCommands;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.AttractionCommands;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionTest {

    private ParkController park;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(out);
        HelpCommands.registerDefault(processor, out);
        AttractionCommands.register(processor, park, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createAttraction() {
        processor.execute("ATTRACTION CREATE;A01;Dragon Rojo;20");
        Attraction a = park.findAttraction("A01");
        assertTrue(output().contains("ATTRACTION CREATED"));
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }

    @Test
    void duplicateIdentifierRejected() {
        processor.execute("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.execute("ATTRACTION CREATE;A01;Otra;5");
        assertTrue(output().contains("ERROR: Attraction A01 already exists"));
        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
        assertEquals(20, park.findAttraction("A01").getCapacity());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A03;Noria;0", "ATTRACTION CREATE;A03;Noria;-2",
        "ATTRACTION CREATE;A03;Noria;diez", "ATTRACTION CREATE;A03;Noria", "ATTRACTION CREATE;A03;;5",
        "ATTRACTION CREATE;A03;Noria;99999999999"})
    void invalidCreationRejected(String command) {
        processor.execute(command);
        assertTrue(output().contains("ERROR:"));
        assertNull(park.findAttraction("A03"));
    }

    @Test
    void capacityBoundsOccupants() {
        Attraction a = park.createAttraction("A02", "Torre del Cielo", 2);
        assertTrue(a.enter("ana@correo.com"));
        assertFalse(a.enter("ana@correo.com"));
        assertTrue(a.enter("pedro@correo.com"));
        assertFalse(a.enter("luis@correo.com"));
        assertTrue(a.isFull());
        assertThrows(UnsupportedOperationException.class, () -> a.getOccupants().add("x"));
    }

    @Test
    void listOccupantsIsReadOnly() {
        Attraction a = park.createAttraction("A01", "Dragon Rojo", 20);
        a.enter("ana@correo.com");
        a.enter("pedro@correo.com");
        processor.execute("ATTRACTION LIST;A01");
        assertTrue(output().contains("ATTRACTION A01 USERS:"));
        assertTrue(output().contains("  ana@correo.com"));
        assertTrue(output().contains("  pedro@correo.com"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), List.copyOf(a.getOccupants()));
    }

    @Test
    void emptyAndUnknownAttraction() {
        park.createAttraction("A01", "Dragon Rojo", 20);
        processor.execute("ATTRACTION LIST;A01");
        processor.execute("ATTRACTION LIST;A99");
        processor.execute("ATTRACTION LIST");
        assertTrue(output().contains("ATTRACTION A01 USERS: (none)"));
        assertTrue(output().contains("ERROR: Attraction A99 does not exist"));
        assertTrue(output().contains("ERROR: Invalid syntax"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void helpListsAttractionCommands() {
        processor.execute("HELP");
        assertTrue(output().contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>"));
        assertTrue(output().contains("ATTRACTION LIST;<attraction>"));
    }
}
