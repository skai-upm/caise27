package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionManagerTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private SystemDate date;
    private TicketManager tickets;
    private AttractionManager attractions;

    @BeforeEach
    void setUp() {
        date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        UserManager users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        GroupManager groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketManager(new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                users, groups, date);
        tickets.create("T001", "ana@correo.com", OCT_15);
        attractions = newManager();
    }

    private AttractionManager newManager() {
        return new AttractionManager(new AttractionRepository(dir.resolve("attractions.txt")), tickets, date);
    }

    @Test
    void createValidatesData() {
        attractions.create("A01", "Tornado", 20);
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", 5));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Otra", 0));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", " ", 5));
        assertEquals(1, attractions.getAttractions().size());
    }

    @Test
    void entryOnlyOnTheTicketDateAndExitWithoutDateCheck() {
        attractions.create("A01", "Tornado", 20);
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T001"));
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        date.set(OCT_15.plusDays(1));
        attractions.exit("A01", "T001");
        assertEquals(0, attractions.findById("A01").getPresent().size());
    }

    @Test
    void deleteRequiresEmptyAttraction() {
        attractions.create("A01", "Tornado", 20);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
        attractions.exit("A01", "T001");
        attractions.delete("A01");
        assertNull(attractions.findById("A01"));
    }

    @Test
    void statusShowsAllInformation() {
        attractions.create("A01", "Tornado", 20);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        String status = attractions.status("A01");
        assertTrue(status.contains("A01") && status.contains("Tornado") && status.contains("Capacity: 20")
                && status.contains("Present: 1") && status.contains("Available: 19") && status.contains("Ana"), status);
    }

    @Test
    void attractionsAreRestoredEmpty() {
        attractions.create("A01", "Tornado", 20);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        Attraction restored = newManager().findById("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertEquals(0, restored.getPresent().size());
    }
}
