package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.model.Pass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * ATTRACTION ENTER with SpeedPasses and Special Passes: all checks happen
 * before anything changes, and entry plus consumption is atomic.
 */
class PassEntryTest {

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
        run("ATTRACTION CREATE;A02;Montana Individual;12;INDIVIDUAL");
        run("ATTRACTION CREATE;A03;Rafting Familiar;12;GROUP");
        run("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private long consumedCount() {
        return park.passes().getPasses().stream().filter(Pass::isConsumed).count();
    }

    private void groupSpeedPasses() {
        run("SPEEDPASS CREATE;SP02;TG01:12345678Z;1");
        run("SPEEDPASS CREATE;SP03;TG01:87654321X;1");
        run("SPEEDPASS CREATE;SP04;TG01:11111111H;1");
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() throws IOException {
        run("SPEEDPASS CREATE;SP01;T001;10");
        assertEquals("ATTRACTION ENTERED (1 user). SpeedPass consumed: SP01:1", run("ATTRACTION ENTER;A02;T001;SPEED"));
        assertEquals(1, consumedCount());
        assertTrue(park.findPass("SP01:1").isConsumed());
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).contains("SPEED;SP01:1;T001;8.00;true"));
        run("ATTRACTION EXIT;A02;ana@correo.com");
        assertEquals("ATTRACTION ENTERED (1 user). SpeedPass consumed: SP01:2", run("ATTRACTION ENTER;A01;T001;speed"));
        assertEquals(10, park.passes().getPasses().size(), "consumed passes stay in the system");
    }

    @Test
    void normalEntryConsumesNothing() {
        run("SPEEDPASS CREATE;SP01;T001;1");
        assertEquals("ATTRACTION ENTERED (1 user)", run("ATTRACTION ENTER;A02;T001"));
        assertEquals(0, consumedCount());
    }

    @Test
    void groupSpeedEntryConsumesOnePassPerMember() {
        groupSpeedPasses();
        run("SPEEDPASS CREATE;SP05;TG01:12345678Z;1");
        assertEquals("ATTRACTION ENTERED (3 users). SpeedPasses consumed: SP02:1, SP03:1, SP04:1",
                run("ATTRACTION ENTER;A03;TG01;SPEED"));
        assertEquals(3, consumedCount());
        assertFalse(park.findPass("SP05:1").isConsumed());
    }

    @Test
    void groupSpeedEntryIsAllOrNothing() {
        run("SPEEDPASS CREATE;SP02;TG01:12345678Z;1");
        run("SPEEDPASS CREATE;SP03;TG01:87654321X;1");
        String output = run("ATTRACTION ENTER;A01;TG01;SPEED");
        assertTrue(output.startsWith("ERROR") && output.contains("TG01:11111111H"), output);
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertEquals(0, consumedCount());
    }

    @Test
    void capacityIsCheckedBeforeConsumingPasses() {
        groupSpeedPasses();
        run("ATTRACTION CREATE;A05;Mini Grupo;2;GROUP");
        assertTrue(run("ATTRACTION ENTER;A05;TG01;SPEED").startsWith("ERROR"));
        assertEquals(0, consumedCount());
    }

    @Test
    void speedEntryWithoutPasses() {
        assertTrue(run("ATTRACTION ENTER;A01;T001;SPEED").startsWith("ERROR"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void specialEntryConsumesTheSpecialPassAutomatically() {
        run("SPECIALPASS CREATE;PE01;T001");
        assertEquals("ATTRACTION ENTERED (1 user). Special Pass consumed: PE01", run("ATTRACTION ENTER;A04;T001"));
        assertTrue(park.findPass("PE01").isConsumed());
        run("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(run("ATTRACTION ENTER;A04;T001").startsWith("ERROR"), "the Special Pass was already used");
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void specialEntryNeedsASpecialPassAndRejectsSpeed() {
        run("SPEEDPASS CREATE;SP01;T001;1");
        assertTrue(run("ATTRACTION ENTER;A04;T001").startsWith("ERROR"));
        run("SPECIALPASS CREATE;PE01;T001");
        String output = run("ATTRACTION ENTER;A04;T001;SPEED");
        assertTrue(output.startsWith("ERROR") && output.contains("does not accept SpeedPasses"), output);
        assertEquals(0, consumedCount());
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void unknownOptionIsRejected() {
        run("SPEEDPASS CREATE;SP01;T001;1");
        assertTrue(run("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR"));
        assertEquals(0, consumedCount());
    }

    @Test
    void failedSaveUndoesEntryAndConsumption() {
        groupSpeedPasses();
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertEquals(0, consumedCount());
    }

    @Test
    void consumedStateSurvivesRestart() {
        run("SPECIALPASS CREATE;PE01;T001");
        run("SPEEDPASS CREATE;SP01;T001;10");
        run("ATTRACTION ENTER;A04;T001");
        run("ATTRACTION EXIT;A04;ana@correo.com");
        run("ATTRACTION ENTER;A02;T001;SPEED");
        Park reopened = TestParks.open(dir);
        assertTrue(reopened.findPass("PE01").isConsumed());
        assertTrue(reopened.findPass("SP01:1").isConsumed());
        assertFalse(reopened.findPass("SP01:2").isConsumed());
        assertTrue(reopened.findAttraction("A02").getOccupants().isEmpty());
        assertEquals(List.of(), reopened.findAttraction("A04").getOccupants());
    }
}
