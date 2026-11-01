package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;

import org.junit.jupiter.api.Test;

class AttractionCommandsTest extends ParkTestSupport {

    private Attraction<?> a01() {
        return park.findAttraction("A01");
    }

    @Test
    void createsAttraction() {
        expect("OK: attraction A01 created", "ATTRACTION CREATE;A01;Tornado;20");
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void rejectsInvalidAttractions() {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        error("ATTRACTION CREATE;A02;Noria;0");
        error("ATTRACTION CREATE;A02;Noria;-3");
        error("ATTRACTION CREATE;A02;Noria;abc");
        error("ATTRACTION CREATE;A02;;5");
        error("ATTRACTION CREATE;A01;Otra;5");
        assertNull(park.findAttraction("A02"));
        assertEquals("Tornado", a01().getName());
    }

    @Test
    void individualEntryRequiresTicketForSystemDate() {
        createUsers(1);
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SYSTEM DATE;2026-10-14");
        error("ATTRACTION ENTER;A01;T001");
        ok("SYSTEM DATE;2026-10-16");
        error("ATTRACTION ENTER;A01;T001");
        ok("SYSTEM DATE;2026-10-15");
        expect("OK: 1 user(s) entered attraction A01", "ATTRACTION ENTER;A01;T001");
        assertEquals(List.of(users.findByEmail(email(1))), a01().getVisitors());
        error("ATTRACTION ENTER;A01;T001");
        error("ATTRACTION ENTER;A99;T001");
        error("ATTRACTION ENTER;A01;T999");
    }

    @Test
    void groupEntryIsAtomic() {
        createUsers(5);
        ok("ATTRACTION CREATE;A01;Tornado;4");
        ok("TICKET CREATE;T005;" + email(5) + ";2026-10-01");
        createGroupWithUsers("G01", 4);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        ok("ATTRACTION ENTER;A01;T005");
        error("ATTRACTION ENTER;A01;TG01");
        assertEquals(1, a01().getVisitors().size());
        ok("ATTRACTION EXIT;A01;T005");
        expect("OK: 4 user(s) entered attraction A01", "ATTRACTION ENTER;A01;TG01");
        assertEquals(0, a01().getAvailablePlaces());
    }

    @Test
    void capacityIsNeverExceeded() {
        createUsers(2);
        ok("ATTRACTION CREATE;A01;Tornado;1");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("TICKET CREATE;T002;" + email(2) + ";2026-10-01");
        ok("ATTRACTION ENTER;A01;T001");
        error("ATTRACTION ENTER;A01;T002");
        assertEquals(1, a01().getVisitors().size());
    }

    @Test
    void groupWithMemberAlreadyInsideEntersNobody() {
        createUsers(2);
        ok("ATTRACTION CREATE;A01;Tornado;10");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        ok("ATTRACTION ENTER;A01;T001");
        error("ATTRACTION ENTER;A01;TG01");
        assertEquals(1, a01().getVisitors().size());
    }

    @Test
    void exitWorksAfterDateChangeAndStatusReflectsState() {
        createUsers(3);
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + email(2));
        ok("GROUP ADD;G01;" + email(3));
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        ok("ATTRACTION ENTER;A01;T001");
        ok("ATTRACTION ENTER;A01;TG01");
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"), status);
        assertTrue(status.contains("Tornado"), status);
        assertTrue(status.contains("Maximum capacity: 20"), status);
        assertTrue(status.contains("Users present: 3"), status);
        assertTrue(status.contains("Available places: 17"), status);
        for (int n = 1; n <= 3; n++) {
            assertTrue(status.contains(email(n)), status);
        }

        ok("SYSTEM DATE;2026-10-05");
        expect("OK: 1 user(s) left attraction A01", "ATTRACTION EXIT;A01;T001");
        status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Users present: 2"), status);
        assertTrue(status.contains("Available places: 18"), status);
        assertTrue(!status.contains(email(1)), status);
        error("ATTRACTION EXIT;A01;T001");
        expect("OK: 2 user(s) left attraction A01", "ATTRACTION EXIT;A01;TG01");
        assertTrue(run("ATTRACTION STATUS;A01").contains("Present: none"));
        error("ATTRACTION STATUS;A99");
    }

    @Test
    void definitionsPersistButAttractionsStartEmpty() throws IOException {
        createUsers(1);
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("ATTRACTION ENTER;A01;T001");
        assertEquals(List.of("A01;Tornado;20;GENERAL"), Files.readAllLines(tempDir.resolve("attractions.txt")));
        restart();
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void deletedUserLeavesAttractions() {
        createUsers(1);
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("ATTRACTION ENTER;A01;T001");
        ok("SYSTEM DATE;2026-10-02");
        ok("USER DELETE;" + email(1) + ";Clave1!");
        assertTrue(a01().getVisitors().isEmpty());
    }
}
