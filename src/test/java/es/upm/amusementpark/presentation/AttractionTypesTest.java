package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAccess;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAccess;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpecialAttraction;
import es.upm.amusementpark.business.model.Ticket;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypesTest {

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
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
        run("ATTRACTION CREATE;A02;Montana Individual;12;individual");
        run("ATTRACTION CREATE;A03;Rafting Familiar;12;GROUP");
        run("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void ticketsImplementTheAccessCapabilities() {
        assertTrue(GeneralAccess.class.isAssignableFrom(Ticket.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertTrue(!GroupAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(!IndividualAccess.class.isAssignableFrom(GroupTicket.class));
    }

    @Test
    void attractionsAreBoundedByTheCapabilityTheyAccept() {
        TypeVariable<?> parameter = Attraction.class.getTypeParameters()[0];
        assertEquals(GeneralAccess.class, parameter.getBounds()[0]);
        assertEquals(GeneralAccess.class, boundOf(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(IndividualAttraction.class));
        assertEquals(GroupAccess.class, boundOf(GroupAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(SpecialAttraction.class));
    }

    private static Object boundOf(Class<?> attraction) {
        return ((ParameterizedType) attraction.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Test
    void typedCreationAndDefault() {
        run("ATTRACTION CREATE;A05;Noria;10");
        assertEquals(AttractionType.GENERAL, park.findAttraction("A05").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(run("ATTRACTION CREATE;A06;Noria;10;VIP").startsWith("ERROR"));
        assertNull(park.findAttraction("A06"));
        assertTrue(run("ATTRACTION STATUS;A03").contains("Type: GROUP"));
    }

    @Test
    void generalAcceptsBothKinds() {
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A01;T001"));
        run("ATTRACTION EXIT;A01;ana@correo.com");
        assertEquals("ATTRACTION ENTERED (3 users)", run("ATTRACTION ENTER;A01;TG01"));
    }

    @Test
    void individualAcceptsOnlyIndividualTickets() {
        String output = run("ATTRACTION ENTER;A02;TG01");
        assertTrue(output.startsWith("ERROR") && output.contains("does not accept group tickets"), output);
        assertTrue(park.findAttraction("A02").getOccupants().isEmpty());
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A02;T001"));
        run("ATTRACTION EXIT;A02;ana@correo.com");
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A02;TG01:87654321X"),
                "an individual ticket inside a group ticket is still an individual ticket");
    }

    @Test
    void groupAcceptsOnlyGroupTickets() {
        String output = run("ATTRACTION ENTER;A03;T001");
        assertTrue(output.startsWith("ERROR") && output.contains("does not accept individual tickets"), output);
        assertTrue(run("ATTRACTION ENTER;A03;TG01:12345678Z").startsWith("ERROR"));
        assertEquals("ATTRACTION ENTERED (3 users)", run("ATTRACTION ENTER;A03;TG01"));
    }

    @Test
    void specialRejectsGroupTickets() {
        assertTrue(run("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void typesSurviveRestartAndUntypedLinesAreGeneral() throws IOException {
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL", "A02;Montana Individual;12;INDIVIDUAL",
                "A03;Rafting Familiar;12;GROUP", "A04;Pasaje del Terror;15;SPECIAL"),
                Files.readAllLines(dir.resolve("attractions.txt")));
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A04;Pasaje del Terror;15;SPECIAL",
                "A07;Mala;5;VIP"));
        Park reopened = TestParks.open(dir);
        assertEquals(AttractionType.GENERAL, reopened.findAttraction("A01").getType());
        assertTrue(reopened.findAttraction("A01") instanceof GeneralAttraction);
        assertEquals(AttractionType.SPECIAL, reopened.findAttraction("A04").getType());
        assertNull(reopened.findAttraction("A07"));
    }
}
