package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassEntryTest extends ParkTestSupport {

    @BeforeEach
    void tickets() {
        createUsers(3);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + email(2));
        ok("GROUP ADD;G01;" + email(3));
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-01");
    }

    private long unused(String ticketId) {
        IndividualTicket ticket = park.findIndividualTicket(ticketId);
        return ticket.getPasses().stream().filter(pass -> !pass.isConsumed()).count();
    }

    private int present(String attractionId) {
        return park.findAttraction(attractionId).getVisitors().size();
    }

    @Test
    void normalEntryConsumesNothing() {
        ok("SPEEDPASS CREATE;T001;10");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        expect("OK: 1 user(s) entered attraction A01", "ATTRACTION ENTER;A01;T001");
        assertEquals(10, unused("T001"));
    }

    @Test
    void speedEntryConsumesExactlyOnePassAndReportsIt() {
        ok("SPEEDPASS CREATE;T001;10");
        ok("ATTRACTION CREATE;A01;Tornado;20;INDIVIDUAL");
        expect("OK: 1 user(s) entered attraction A01 (consumed: SpeedPass T001-SP1)", "ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(9, unused("T001"));
        ok("ATTRACTION EXIT;A01;T001");
        expect("OK: 1 user(s) entered attraction A01 (consumed: SpeedPass T001-SP2)", "ATTRACTION ENTER;A01;T001;SPEED");
    }

    @Test
    void speedEntryWithoutPassOrInvalidOptionIsRejected() {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        error("ATTRACTION ENTER;A01;T001;SPEED");
        ok("SPEEDPASS CREATE;T001;1");
        error("ATTRACTION ENTER;A01;T001;FAST");
        assertEquals(0, present("A01"));
        assertEquals(1, unused("T001"));
    }

    @Test
    void failedEntryConsumesNoPass() {
        ok("SPEEDPASS CREATE;T001;1");
        ok("ATTRACTION CREATE;A01;Tornado;2");
        ok("ATTRACTION ENTER;A01;TG01");
        error("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(1, unused("T001"));
        ok("SYSTEM DATE;2026-10-02");
        error("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(1, unused("T001"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        ok("ATTRACTION CREATE;A01;Tornado;20;GROUP");
        ok("SPEEDPASS CREATE;TG01-1;1");
        error("ATTRACTION ENTER;A01;TG01;SPEED");
        assertEquals(0, present("A01"));
        assertEquals(1, unused("TG01-1"));

        ok("SPEEDPASS CREATE;TG01-2;1");
        String output = run("ATTRACTION ENTER;A01;TG01;SPEED");
        assertEquals("OK: 2 user(s) entered attraction A01 (consumed: SpeedPass TG01-1-SP1, SpeedPass TG01-2-SP1)",
                output);
        assertEquals(0, unused("TG01-1"));
        assertEquals(0, unused("TG01-2"));
    }

    @Test
    void groupSpeedEntryFailingOnCapacityConsumesNothing() {
        ok("ATTRACTION CREATE;A01;Tornado;1");
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SPEEDPASS CREATE;TG01-2;1");
        error("ATTRACTION ENTER;A01;TG01;SPEED");
        assertEquals(1, unused("TG01-1"));
        assertEquals(1, unused("TG01-2"));
    }

    @Test
    void specialAttractionConsumesSpecialPassAutomatically() {
        ok("ATTRACTION CREATE;A02;Casa Magica;5;SPECIAL");
        error("ATTRACTION ENTER;A02;T001");
        ok("SPECIALPASS CREATE;T001");
        ok("SPEEDPASS CREATE;T001;1");
        error("ATTRACTION ENTER;A02;T001;SPEED");
        assertEquals(2, unused("T001"));
        expect("OK: 1 user(s) entered attraction A02 (consumed: Special Pass T001-SPECIAL)", "ATTRACTION ENTER;A02;T001");
        assertTrue(park.findIndividualTicket("T001").getSpecialPass().isConsumed());
        assertEquals(1, unused("T001"));
        ok("ATTRACTION EXIT;A02;T001");
        error("ATTRACTION ENTER;A02;T001");
        error("ATTRACTION ENTER;A02;TG01");
    }

    @Test
    void constituentSpecialPassUsableAtSpecialAttraction() {
        ok("ATTRACTION CREATE;A02;Casa Magica;5;SPECIAL");
        ok("SPECIALPASS CREATE;TG01-1");
        expect("OK: 1 user(s) entered attraction A02 (consumed: Special Pass TG01-1-SPECIAL)",
                "ATTRACTION ENTER;A02;TG01-1");
    }

    @Test
    void failedSaveUndoesConsumingEntry() throws IOException {
        ok("SPEEDPASS CREATE;T001;1");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        Files.delete(tempDir.resolve("tickets.txt"));
        Files.createDirectory(tempDir.resolve("tickets.txt"));
        error("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(0, present("A01"));
        Pass pass = park.findIndividualTicket("T001").getSpeedPasses().get(0);
        assertFalse(pass.isConsumed());
    }
}
