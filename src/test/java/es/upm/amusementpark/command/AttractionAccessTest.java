package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.command.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.service.Park;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionAccessTest {

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
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private List<String> occupants(String attractionId) {
        return park.findAttraction(attractionId).getOccupants().stream().map(u -> u.getEmail()).toList();
    }

    @Test
    void entersWithIndividualTicketAndLeaves() {
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A02;T001"));
        assertEquals(List.of("ana@correo.com"), occupants("A02"));
        assertEquals("ana@correo.com", run("ATTRACTION LIST;A02"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A02;ana@correo.com"));
        assertEquals("NO USERS IN ATTRACTION A02", run("ATTRACTION LIST;A02"));
    }

    @Test
    void ticketValidOnlyOnItsDate() {
        run("TICKET CREATE;T002;pedro@correo.com;2027-04-20");
        assertTrue(run("ATTRACTION ENTER;A01;T002").startsWith("ERROR"));
        run("SYSTEM DATE;2027-04-16");
        assertTrue(run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(occupants("A01").isEmpty());
        run("SYSTEM DATE;2027-04-20");
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A01;T002"));
    }

    @Test
    void rejectsUnknownTicketOrAttraction() {
        assertTrue(run("ATTRACTION ENTER;A01;NOPE").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(run("ATTRACTION EXIT;A99;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("ATTRACTION EXIT;A01;ana@correo.com").startsWith("ERROR"));
    }

    @Test
    void userCannotBeInsideTwice() {
        run("ATTRACTION ENTER;A01;T001");
        assertTrue(run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A02;T001").startsWith("ERROR"));
        assertEquals(1, occupants("A01").size());
        run("ATTRACTION EXIT;A01;ana@correo.com");
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A02;T001"));
    }

    @Test
    void presenceIsIndependentOfLaterDateChanges() {
        run("ATTRACTION ENTER;A02;T001");
        run("SYSTEM DATE;2027-04-16");
        assertEquals(List.of("ana@correo.com"), occupants("A02"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A02;ANA@correo.com"));
    }

    @Test
    void deletedUserLeavesAttractionAndOldTicketCannotBeReused() {
        run("ATTRACTION ENTER;A02;T001");
        run("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", run("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(occupants("A02").isEmpty());
        run("SYSTEM DATE;2027-04-15");
        assertTrue(run("ATTRACTION ENTER;A02;T001").startsWith("ERROR"));
    }

    @Test
    void occupantsAreNotPersisted() {
        run("ATTRACTION ENTER;A01;T001");
        Park reopened = TestParks.open(dir, LocalDate.of(2027, 4, 15));
        Attraction attraction = reopened.findAttraction("A01");
        assertTrue(attraction.getOccupants().isEmpty());
        assertEquals(20, attraction.getFreePlaces());
    }
}
