package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AttractionTest extends ParkTestSupport {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    private Attraction attraction(String id) {
        return controller.getAttractionManager().get(id);
    }

    private static User person(String dni) {
        return new User("N", "S", dni, dni + "@correo.com", "Clave1!", "600000000", "1234567890123456");
    }

    @Test
    public void groupEntryIsAtomicWhenCapacityIsShort() {
        Attraction small = new Attraction("A01", "Tornado", 3);
        List<IndividualTicket> four = List.of(
                new IndividualTicket("a", person("1"), DAY, BigDecimal.TEN),
                new IndividualTicket("b", person("2"), DAY, BigDecimal.TEN),
                new IndividualTicket("c", person("3"), DAY, BigDecimal.TEN),
                new IndividualTicket("d", person("4"), DAY, BigDecimal.TEN));
        GroupTicket group = new GroupTicket("TG", new Group("G", "g"), DAY, BigDecimal.TEN, four);
        assertThrows(ParkException.class, () -> small.enter(group));
        assertTrue(small.getPresent().isEmpty());
        assertEquals(1, small.enter(four.get(0)));
        assertThrows(ParkException.class, () -> small.enter(four.get(0)));
        assertEquals(2, small.getAvailable());
    }

    @Test
    public void createsAndDeletesAttractions() {
        assertEquals("OK ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Tornado;20"));
        assertEquals(20, attraction("A01").getCapacity());
        assertTrue(attraction("A01").getPresent().isEmpty());
        fails("ATTRACTION CREATE;A02;Rio;0");
        fails("ATTRACTION CREATE;A02;Rio;diez");
        fails("ATTRACTION CREATE;A01;Otra;5");
        fails("ATTRACTION CREATE;A03;;5");
        assertEquals("OK ATTRACTION DELETED", run("ATTRACTION DELETE;A01"));
        fails("ATTRACTION DELETE;A01");
    }

    @Test
    public void entersExitsAndReportsStatus() {
        createAna();
        String[] members = createUsers(3);
        ok("GROUP CREATE;G01;Grupo");
        for (String email : members) {
            ok("GROUP ADD;G01;" + email);
        }
        ok("SYSTEM DATE;2026-10-01");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;20");

        fails("ATTRACTION ENTER;A01;T001");
        ok("SYSTEM DATE;2026-10-15");
        assertEquals("OK ATTRACTION ENTER -> 1", run("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK ATTRACTION ENTER -> 3", run("ATTRACTION ENTER;A01;TG01"));
        fails("ATTRACTION ENTER;A01;T001");
        fails("ATTRACTION DELETE;A01");

        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01 - Tornado"), status);
        assertTrue(status.contains("Capacity: 20"), status);
        assertTrue(status.contains("Present: 4"), status);
        assertTrue(status.contains("Available: 16"), status);
        assertTrue(status.contains(ANA) && status.contains(members[0]) && status.contains(members[2]), status);

        assertEquals("OK ATTRACTION EXIT -> 1", run("ATTRACTION EXIT;A01;T001"));
        fails("ATTRACTION EXIT;A01;T001");
        status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 3") && status.contains("Available: 17"), status);
        assertTrue(!status.contains(ANA), status);

        ok("SYSTEM DATE;2026-10-16");
        assertEquals("OK ATTRACTION EXIT -> 3", run("ATTRACTION EXIT;A01;TG01"));
        assertTrue(run("ATTRACTION STATUS;A01").contains("Users present: none"));
    }

    @Test
    public void groupWithoutEnoughPlacesDoesNotEnter() {
        createAna();
        String[] members = createUsers(4);
        ok("GROUP CREATE;G01;Grupo");
        for (String email : members) {
            ok("GROUP ADD;G01;" + email);
        }
        ok("SYSTEM DATE;2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;4");
        ok("ATTRACTION ENTER;A01;T001");
        fails("ATTRACTION ENTER;A01;TG01");
        assertEquals(1, attraction("A01").getPresent().size());
    }

    @Test
    public void unknownReferencesAreRejected() {
        ok("ATTRACTION CREATE;A01;Tornado;4");
        fails("ATTRACTION ENTER;A01;T999");
        fails("ATTRACTION ENTER;A99;T001");
        fails("ATTRACTION STATUS;A99");
    }

    @Test
    public void ticketOfDeletedUserCannotEnter() {
        createAna();
        ok("SYSTEM DATE;2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SYSTEM DATE;2026-10-16");
        ok("USER DELETE;" + ANA + ";Clave1!");
        ok("SYSTEM DATE;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;4");
        fails("ATTRACTION ENTER;A01;T001");
    }

    @Test
    public void attractionsSurviveRestartEmpty() throws Exception {
        createAna();
        ok("SYSTEM DATE;2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION ENTER;A01;T001");
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dataDir.resolve(AttractionRepository.FILE_NAME)));
        restart();
        assertEquals("Tornado", attraction("A01").getName());
        assertEquals(20, attraction("A01").getCapacity());
        assertTrue(attraction("A01").getPresent().isEmpty());
    }

    @Test
    public void malformedAttractionLinesAreSkipped() throws Exception {
        Files.write(dataDir.resolve(AttractionRepository.FILE_NAME), List.of("A01;Tornado;x", "A02;Rio;5", "A03"));
        restart();
        assertEquals(1, controller.getAttractionManager().getAttractions().size());
    }
}
