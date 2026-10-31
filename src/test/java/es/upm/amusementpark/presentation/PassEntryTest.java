package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassEntryTest extends ParkTestSupport {

    @BeforeEach
    void park() {
        run(ANA);
        run(PEDRO);
        createUser(3);
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;u3@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        run("ATTRACTION CREATE;GEN;General;10");
        run("ATTRACTION CREATE;IND;Individual;10;INDIVIDUAL");
        run("ATTRACTION CREATE;GRP;Grupo;10;GROUP");
        run("ATTRACTION CREATE;SPE;Especial;10;SPECIAL");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) park.getTickets().find(id);
    }

    private long unusedSpeedPasses(String ticketId) {
        return ticket(ticketId).getSpeedPasses().stream().filter(p -> !p.isConsumed()).count();
    }

    private int present(String attractionId) {
        return park.getAttractions().find(attractionId).getVisitors().size();
    }

    @Test
    void normalEntryIsUnchangedAndConsumesNothing() {
        run("SPEEDPASS CREATE;T001;1");
        assertEquals("OK: ticket T001 entered attraction GEN", run("ATTRACTION ENTER;GEN;T001"));
        assertEquals(1, unusedSpeedPasses("T001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GEN", "IND"})
    void individualSpeedEntryConsumesExactlyOnePass(String attractionId) {
        run("SPEEDPASS CREATE;T001;10");
        assertEquals("OK: ticket T001 entered attraction " + attractionId + " (consumed: SpeedPass T001-SPEED1)",
                run("ATTRACTION ENTER;" + attractionId + ";T001;SPEED"));
        assertEquals(9, unusedSpeedPasses("T001"));
    }

    @Test
    void individualSpeedEntryWithoutPassFails() {
        assertTrue(error(run("ATTRACTION ENTER;GEN;T001;SPEED")));
        assertEquals(0, present("GEN"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GEN", "GRP"})
    void groupSpeedEntryConsumesOnePassPerMember(String attractionId) {
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPEEDPASS CREATE;TG01-2;10");
        String result = run("ATTRACTION ENTER;" + attractionId + ";TG01;SPEED");
        assertTrue(result.contains("SpeedPass TG01-1-SPEED1") && result.contains("SpeedPass TG01-2-SPEED1"), result);
        assertEquals(2, present(attractionId));
        assertEquals(0, unusedSpeedPasses("TG01-1"));
        assertEquals(9, unusedSpeedPasses("TG01-2"));
    }

    @Test
    void groupSpeedEntryIsAtomicWhenAMemberLacksAPass() {
        run("SPEEDPASS CREATE;TG01-1;1");
        assertTrue(error(run("ATTRACTION ENTER;GEN;TG01;SPEED")));
        assertEquals(0, present("GEN"));
        assertEquals(1, unusedSpeedPasses("TG01-1"));
    }

    @Test
    void failedEntryForOtherReasonsConsumesNoPass() {
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPEEDPASS CREATE;TG01-2;1");
        run("ATTRACTION CREATE;MINI;Mini;1");
        assertTrue(error(run("ATTRACTION ENTER;MINI;TG01;SPEED")));
        assertTrue(error(run("ATTRACTION ENTER;IND;TG01;SPEED")));
        run("SYSTEM DATE;2026-10-02");
        assertTrue(error(run("ATTRACTION ENTER;GEN;TG01;SPEED")));
        assertEquals(1, unusedSpeedPasses("TG01-1"));
        assertEquals(1, unusedSpeedPasses("TG01-2"));
    }

    @Test
    void specialAttractionConsumesTheSpecialPass() throws IOException {
        run("SPECIALPASS CREATE;T001");
        assertEquals("OK: ticket T001 entered attraction SPE (consumed: Special Pass T001-SPECIAL)",
                run("ATTRACTION ENTER;SPE;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());

        restart();
        Pass pass = ticket("T001").getSpecialPass();
        assertTrue(pass.isConsumed());
        assertTrue(error(run("ATTRACTION ENTER;SPE;T001")));
    }

    @Test
    void specialAttractionRequiresAnUnusedSpecialPass() {
        assertTrue(error(run("ATTRACTION ENTER;SPE;T001")));
        assertEquals(0, present("SPE"));
    }

    @Test
    void specialAttractionRejectsSpeed() {
        run("SPECIALPASS CREATE;T001");
        run("SPEEDPASS CREATE;T001;1");
        assertTrue(error(run("ATTRACTION ENTER;SPE;T001;SPEED")));
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertEquals(1, unusedSpeedPasses("T001"));
        assertEquals(0, present("SPE"));
    }

    @Test
    void consumedSpeedPassesPersist() throws IOException {
        run("SPEEDPASS CREATE;T001;10");
        run("ATTRACTION ENTER;GEN;T001;SPEED");
        restart();
        assertEquals(9, unusedSpeedPasses("T001"));
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
    }

    @Test
    void consumedPassIsNotRefundable() {
        run("SYSTEM DATE;2026-09-30");
        run("TICKET CREATE;T009;ana@correo.com;2026-10-05");
        run("SPEEDPASS CREATE;T009;1");
        ticket("T009").getSpeedPasses().get(0).consume();
        assertTrue(error(run("SPEEDPASS DELETE;T009-SPEED1")));
    }

    @Test
    void rejectsUnknownEnterOption() {
        assertTrue(error(run("ATTRACTION ENTER;GEN;T001;FAST")));
        assertEquals(0, present("GEN"));
    }
}
