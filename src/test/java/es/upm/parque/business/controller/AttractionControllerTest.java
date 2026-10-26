package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileAttractionRepository;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionControllerTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private SystemDate date;
    private TicketController tickets;
    private AttractionController attractions;

    @BeforeEach
    void setUp() {
        date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        UserController users = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")), new BCryptPasswordEncoder(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        GroupController groups = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(new FileTicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                users, groups, date);
        tickets.create("T001", "ana@correo.com", OCT_15);
        attractions = newController();
    }

    private AttractionController newController() {
        return AttractionController.getInstance().configure(new FileAttractionRepository(dir.resolve("attractions.txt")), tickets, date);
    }

    @Test
    void createValidatesData() {
        attractions.create("A01", "Tornado", 20, AttractionType.GENERAL);
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", 5, AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Otra", 0, AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", " ", 5, AttractionType.GENERAL));
        assertEquals(1, attractions.getAttractions().size());
    }

    @Test
    void entryOnlyOnTheTicketDateAndExitWithoutDateCheck() {
        attractions.create("A01", "Tornado", 20, AttractionType.GENERAL);
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T001"));
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        date.set(OCT_15.plusDays(1));
        attractions.exit("A01", "T001");
        assertEquals(0, attractions.findById("A01").getPresent().size());
    }

    @Test
    void deleteRequiresEmptyAttraction() {
        attractions.create("A01", "Tornado", 20, AttractionType.GENERAL);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
        attractions.exit("A01", "T001");
        attractions.delete("A01");
        assertNull(attractions.findById("A01"));
    }

    @Test
    void statusShowsAllInformation() {
        attractions.create("A01", "Tornado", 20, AttractionType.GENERAL);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        String status = attractions.status("A01");
        assertTrue(status.contains("A01") && status.contains("Tornado") && status.contains("Capacity: 20")
                && status.contains("Present: 1") && status.contains("Available: 19") && status.contains("Ana"), status);
    }

    @Test
    void attractionsAreRestoredEmpty() {
        attractions.create("A01", "Tornado", 20, AttractionType.GENERAL);
        date.set(OCT_15);
        attractions.enter("A01", "T001");
        Attraction restored = newController().findById("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertEquals(0, restored.getPresent().size());
    }

    @Test
    void typeIsShownAndPersisted() throws Exception {
        attractions.create("A01", "Tornado", 20, AttractionType.SPECIAL);
        assertTrue(attractions.status("A01").contains("Type: SPECIAL"));
        assertEquals(AttractionType.SPECIAL, newController().findById("A01").getType());
        java.nio.file.Files.writeString(dir.resolve("attractions.txt"), "A09;Antigua;5\n");
        assertEquals(AttractionType.GENERAL, newController().findById("A09").getType());
    }
}
