package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupAttractionAccessTest {

    private static final String NL = System.lineSeparator();

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
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private List<String> occupants(String attractionId) {
        return park.findAttraction(attractionId).getOccupants().stream().map(u -> u.getEmail()).toList();
    }

    @Test
    void groupEntersTogetherAndLeavesIndividually() {
        assertEquals("ATTRACTION ENTERED (3 users)", run("ATTRACTION ENTER;A01;TG01"));
        assertEquals("ana@correo.com" + NL + "pedro@correo.com" + NL + "laura@correo.com", run("ATTRACTION LIST;A01"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals(List.of("pedro@correo.com", "laura@correo.com"), occupants("A01"));
        assertEquals(String.join(NL,
                "Attraction: A01",
                "Name: Dragon Rojo",
                "Type: GENERAL",
                "Capacity: 20",
                "Users inside: 2",
                "Free places: 18",
                "Users: pedro@correo.com, laura@correo.com"), run("ATTRACTION STATUS;A01"));
    }

    @Test
    void insufficientCapacityAdmitsNobody() {
        run("ATTRACTION CREATE;A03;Mini;2");
        String output = run("ATTRACTION ENTER;A03;TG01");
        assertTrue(output.startsWith("ERROR"));
        assertTrue(occupants("A03").isEmpty());
        run("ATTRACTION CREATE;A04;Justo;3");
        assertEquals("ATTRACTION ENTERED (3 users)", run("ATTRACTION ENTER;A04;TG01"));
        assertTrue(run("ATTRACTION STATUS;A04").contains("Free places: 0"));
    }

    @Test
    void groupEntryFailsIfAnyMemberIsAlreadyInside() {
        run("TICKET CREATE;T001;pedro@correo.com;2027-04-15");
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        run("ATTRACTION ENTER;A02;T001");
        assertTrue(run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(occupants("A01").isEmpty());
    }

    @Test
    void componentTicketAdmitsOnlyThatMember() {
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A01;TG01:87654321X"));
        assertEquals(List.of("pedro@correo.com"), occupants("A01"));
    }

    @Test
    void statusOfEmptyAndUnknownAttraction() {
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        assertEquals(String.join(NL,
                "Attraction: A02",
                "Name: Torre del Cielo",
                "Type: GENERAL",
                "Capacity: 12",
                "Users inside: 0",
                "Free places: 12",
                "Users: none"), run("ATTRACTION STATUS;A02"));
        assertTrue(run("ATTRACTION STATUS;A99").startsWith("ERROR"));
    }

    @Test
    void listAndStatusDoNotChangeState() {
        run("ATTRACTION ENTER;A01;TG01");
        run("ATTRACTION LIST;A01");
        run("ATTRACTION STATUS;A01");
        assertEquals(3, occupants("A01").size());
    }
}
