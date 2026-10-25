package es.upm.parque;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.AccessCredential;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GroupAccess;
import es.upm.parque.business.model.GroupAttraction;
import es.upm.parque.business.model.IndividualAccess;
import es.upm.parque.business.model.IndividualAttraction;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.persistence.TextAttractionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AttractionTypeTest extends ParkTestSupport {

    private String[] members;

    @BeforeEach
    void setUpPark() {
        createAna();
        members = createUsers(3);
        ok("GROUP CREATE;G01;Grupo");
        for (String email : members) {
            ok("GROUP ADD;G01;" + email);
        }
        ok("SYSTEM DATE;2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION CREATE;A02;Familiar;20;GROUP");
        ok("ATTRACTION CREATE;A03;Solo;20;individual");
        ok("ATTRACTION CREATE;A04;Castillo;20;SPECIAL");
    }

    private long consumed() {
        return passes().getPasses().stream().filter(Pass::isConsumed).count();
    }

    private int present(String attractionId) {
        return attractions().get(attractionId).getPresent().size();
    }

    @Test
    public void createsTypedAndDefaultAttractions() throws Exception {
        assertEquals(AttractionType.GENERAL, attractions().get("A01").getType());
        assertEquals(AttractionType.GROUP, attractions().get("A02").getType());
        assertEquals(AttractionType.INDIVIDUAL, attractions().get("A03").getType());
        assertEquals(AttractionType.SPECIAL, attractions().get("A04").getType());
        fails("ATTRACTION CREATE;A05;Rio;10;WATER");
        assertTrue(run("ATTRACTION STATUS;A04").contains("Type: SPECIAL"));
        restart();
        assertEquals(AttractionType.SPECIAL, attractions().get("A04").getType());
        assertTrue(Files.readAllLines(dataDir.resolve(TextAttractionRepository.FILE_NAME)).contains("A02;Familiar;20;GROUP"));
    }

    @Test
    public void legacyLinesLoadAsGeneral() throws Exception {
        Files.write(dataDir.resolve(TextAttractionRepository.FILE_NAME), List.of("A09;Antigua;5", "A10;Mala;5;RARA"));
        restart();
        assertEquals(AttractionType.GENERAL, attractions().get("A09").getType());
        assertEquals(1, attractions().getAttractions().size());
    }

    @Test
    public void typesRestrictTheTicketsTheyAdmit() {
        fails("ATTRACTION ENTER;A02;T001");
        fails("ATTRACTION ENTER;A03;TG01");
        fails("ATTRACTION ENTER;A04;TG01");
        assertEquals(0, present("A02") + present("A03") + present("A04"));
        assertEquals("OK ATTRACTION ENTER -> 3", run("ATTRACTION ENTER;A02;TG01"));
        assertEquals("OK ATTRACTION ENTER -> 1", run("ATTRACTION ENTER;A03;T001"));
        assertEquals("OK ATTRACTION ENTER -> 1", run("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK ATTRACTION ENTER -> 3", run("ATTRACTION ENTER;A01;TG01"));
    }

    @Test
    public void genericBoundsTypeTheCredential() {
        IndividualTicket t001 = tickets().findIndividual("T001");
        IndividualAttraction solo = new IndividualAttraction("X", "x", 5);
        IndividualAccess typed = solo.admit(t001);
        assertSame(t001, typed);
        GroupAttraction familiar = new GroupAttraction("Y", "y", 5);
        assertThrows(ParkException.class, () -> familiar.admit(t001));
        AccessCredential group = tickets().find("TG01");
        GroupAccess groupAccess = familiar.admit(group);
        assertEquals("G01", groupAccess.getGroup().getId());
    }

    @Test
    public void speedEntryConsumesExactlyOnePassPerTicket() {
        ok("SPEEDPASS CREATE;T001;10");
        String output = run("ATTRACTION ENTER;A03;T001;SPEED");
        assertEquals("OK ATTRACTION ENTER -> 1 (SpeedPass T001-SP1 consumed)", output);
        assertEquals(1, consumed());
        fails("ATTRACTION ENTER;A04;T001;SPEED");
        fails("ATTRACTION ENTER;A01;T001;FAST");
        assertEquals(1, consumed());
    }

    @Test
    public void groupSpeedEntryIsAtomic() {
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SPEEDPASS CREATE;TG01-2;1");
        fails("ATTRACTION ENTER;A02;TG01;SPEED");
        assertEquals(0, present("A02"));
        assertEquals(0, consumed());
        ok("SPEEDPASS CREATE;TG01-3;1");
        assertEquals("OK ATTRACTION ENTER -> 3 (SpeedPass TG01-1-SP1, TG01-2-SP1, TG01-3-SP1 consumed)",
                run("ATTRACTION ENTER;A02;TG01;SPEED"));
        assertEquals(3, consumed());
    }

    @Test
    public void failedEntryConsumesNothing() {
        ok("SPEEDPASS CREATE;T001;1");
        ok("ATTRACTION CREATE;A06;Mini;1");
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SPEEDPASS CREATE;TG01-2;1");
        ok("SPEEDPASS CREATE;TG01-3;1");
        fails("ATTRACTION ENTER;A06;TG01;SPEED");
        ok("SYSTEM DATE;2026-10-16");
        fails("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(0, consumed());
    }

    @Test
    public void specialAttractionConsumesTheSpecialPass() {
        fails("ATTRACTION ENTER;A04;T001");
        ok("SPECIALPASS CREATE;T001");
        assertEquals("OK ATTRACTION ENTER -> 1 (Special Pass T001-SPECIAL consumed)", run("ATTRACTION ENTER;A04;T001"));
        assertTrue(passes().find("T001-SPECIAL").isConsumed());
        ok("ATTRACTION EXIT;A04;T001");
        fails("ATTRACTION ENTER;A04;T001");
        assertEquals(0, present("A04"));
    }

    @Test
    public void specialPassIsNotConsumedWhenEntryFails() {
        ok("SPECIALPASS CREATE;T001");
        ok("ATTRACTION CREATE;A07;Lleno;1;SPECIAL");
        ok("SPECIALPASS CREATE;TG01-1");
        ok("ATTRACTION ENTER;A07;TG01-1");
        fails("ATTRACTION ENTER;A07;T001");
        assertFalse(passes().find("T001-SPECIAL").isConsumed());
    }
}
