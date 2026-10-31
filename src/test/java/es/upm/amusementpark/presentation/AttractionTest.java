package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.model.Attraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionTest extends ParkTestSupport {

    @BeforeEach
    void setUpPark() {
        run(ANA);
        run(PEDRO);
        createUser(3);
        createUser(4);
        run("GROUP CREATE;G01;Grupo");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;u3@correo.com");
        run("ATTRACTION CREATE;A01;Tornado;20");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-01");
    }

    private Attraction<?> a01() {
        return park.getAttractions().find("A01");
    }

    @Test
    void createsAttraction() {
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A02;Noria;0", "ATTRACTION CREATE;A02;Noria;diez",
            "ATTRACTION CREATE;A02;Noria;-3", "ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;A02; ;5"})
    void rejectsInvalidAttractions(String command) {
        assertTrue(error(run(command)));
        assertEquals(1, park.getAttractions().getAttractions().size());
        assertEquals("Tornado", a01().getName());
    }

    @Test
    void individualAndGroupEntryThenExitAndStatus() {
        assertTrue(ok(run("ATTRACTION ENTER;A01;T001")));
        assertTrue(ok(run("ATTRACTION ENTER;A01;TG01")));

        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Tornado"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Users present: 3"));
        assertTrue(status.contains("Available places: 17"));
        assertTrue(status.contains("ana@correo.com"));
        assertTrue(status.contains("pedro@correo.com"));
        assertTrue(status.contains("u3@correo.com"));

        assertTrue(ok(run("ATTRACTION EXIT;A01;T001")));
        status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Users present: 2"));
        assertTrue(status.contains("Available places: 18"));
        assertFalse(status.contains("ana@correo.com"));
    }

    @Test
    void groupEntryIsAtomicWhenCapacityIsShort() {
        run("ATTRACTION CREATE;A02;Mini;3");
        run("ATTRACTION ENTER;A02;T001");
        run("TICKET CREATE;T004;u4@correo.com;2026-10-01");
        run("ATTRACTION ENTER;A02;T004");
        assertTrue(error(run("ATTRACTION ENTER;A02;TG01")));
        assertEquals(2, park.getAttractions().find("A02").getVisitors().size());
    }

    @Test
    void capacityIsNeverExceeded() {
        run("ATTRACTION CREATE;A02;Mini;1");
        assertTrue(ok(run("ATTRACTION ENTER;A02;T001")));
        run("TICKET CREATE;T004;u4@correo.com;2026-10-01");
        assertTrue(error(run("ATTRACTION ENTER;A02;T004")));
    }

    @Test
    void ticketMustBeForTheSystemDate() {
        run("TICKET CREATE;T002;u4@correo.com;2026-10-02");
        assertTrue(error(run("ATTRACTION ENTER;A01;T002")));
        run("SYSTEM DATE;2026-10-02");
        assertTrue(error(run("ATTRACTION ENTER;A01;T001")));
        assertTrue(ok(run("ATTRACTION ENTER;A01;T002")));
    }

    @Test
    void userAlreadyPresentCannotEnterAgain() {
        run("ATTRACTION ENTER;A01;TG01-1");
        assertTrue(error(run("ATTRACTION ENTER;A01;TG01")));
        assertEquals(1, a01().getVisitors().size());
    }

    @Test
    void exitDoesNotRevalidateDate() {
        run("ATTRACTION ENTER;A01;TG01");
        run("SYSTEM DATE;2026-10-05");
        assertTrue(ok(run("ATTRACTION EXIT;A01;TG01")));
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void rejectsExitWhenNobodyPresentOrUnknownIds() {
        assertTrue(error(run("ATTRACTION EXIT;A01;T001")));
        assertTrue(error(run("ATTRACTION EXIT;A99;T001")));
        assertTrue(error(run("ATTRACTION ENTER;A01;NOPE")));
        assertTrue(error(run("ATTRACTION STATUS;A99")));
    }

    @Test
    void definitionsPersistButAttractionsStartEmpty() throws IOException {
        run("ATTRACTION ENTER;A01;T001");
        restart();
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void deletedUserLeavesAttractions() {
        run("ATTRACTION ENTER;A01;T001");
        run("SYSTEM DATE;2026-10-02");
        assertTrue(ok(run("USER DELETE;ana@correo.com;Clave1!")));
        assertTrue(a01().getVisitors().isEmpty());
    }
}
