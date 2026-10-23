package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.controller.Park;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionCommandsTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
    }

    String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createsAttraction() {
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        Attraction<?> attraction = park.findAttraction("A01");
        assertNotNull(attraction);
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "diez", "", "1.5", "9999999999", "+3"})
    void rejectsInvalidCapacity(String capacity) {
        assertTrue(run("ATTRACTION CREATE;A03;Noria;" + capacity).startsWith("ERROR"));
        assertNull(park.findAttraction("A03"));
    }

    @Test
    void rejectsDuplicatesAndEmptyFields() {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertTrue(run("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertTrue(run("ATTRACTION CREATE;;Otra;5").startsWith("ERROR"));
        assertTrue(run("ATTRACTION CREATE;A02;;5").startsWith("ERROR"));
        assertEquals(1, park.getAttractions().size());
    }

    @Test
    void listOfEmptyAttraction() {
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        assertEquals("NO USERS IN ATTRACTION A02", run("ATTRACTION LIST;A02"));
        assertTrue(run("ATTRACTION LIST;A99").startsWith("ERROR"));
    }

    @Test
    void attractionsSurviveRestart() throws IOException {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL", "A02;Torre del Cielo;12;GENERAL"),
                Files.readAllLines(dir.resolve("attractions.txt")));
        park = TestParks.open(dir);
        assertEquals(12, park.findAttraction("A02").getCapacity());
    }

    @Test
    void malformedAttractionLinesAreSkipped() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A01;Dup;3", "A02;Bad;0",
                "A03;Few", "", "A04;Bien;4"));
        park = TestParks.open(dir);
        assertEquals(2, park.getAttractions().size());
        assertNotNull(park.findAttraction("A04"));
    }
}
