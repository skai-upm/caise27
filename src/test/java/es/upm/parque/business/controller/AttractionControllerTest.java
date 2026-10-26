package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionControllerTest {

    @TempDir
    Path dir;

    private final SystemDate date = new SystemDate(LocalDate.of(2026, 10, 15));
    private TicketController tickets;
    private AttractionController attractions;

    @BeforeEach
    void setUp() throws IOException {
        UserController users = UserController.getInstance().configure(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        GroupController groups = GroupController.getInstance().configure(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                users, groups, date);
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        attractions = newManager();
    }

    private AttractionController newManager() throws IOException {
        return AttractionController.getInstance().configure(new AttractionRepository(dir.resolve("attractions.txt")), tickets, date);
    }

    @Test
    void createValidations() {
        attractions.create("A01", "Tornado", "20");
        assertThrows(BusinessException.class, () -> attractions.create("A01", "Otra", "5"));
        assertThrows(BusinessException.class, () -> attractions.create("", "Otra", "5"));
        assertThrows(BusinessException.class, () -> attractions.create("A02", "", "5"));
        assertThrows(BusinessException.class, () -> attractions.create("A02", "Noria", "-1"));
        assertThrows(BusinessException.class, () -> attractions.create("A02", "Noria", "x"));
        assertEquals(1, attractions.getAttractions().size());
    }

    @Test
    void enterExitUseTheSystemDate() {
        attractions.create("A01", "Tornado", "20");
        date.set(LocalDate.of(2026, 10, 14));
        assertThrows(BusinessException.class, () -> attractions.enter("A01", "T001"));
        date.set(LocalDate.of(2026, 10, 15));
        assertEquals(1, attractions.enter("A01", "T001"));
        date.set(LocalDate.of(2026, 10, 16));
        assertEquals(1, attractions.exit("A01", "T001"));
        assertThrows(BusinessException.class, () -> attractions.enter("A99", "T001"));
        assertThrows(BusinessException.class, () -> attractions.enter("A01", "T999"));
    }

    @Test
    void attractionsPersistAndStartEmpty() throws IOException {
        attractions.create("A01", "Tornado", "20");
        attractions.enter("A01", "T001");
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));

        AttractionController restarted = newManager();
        Attraction restored = restarted.find("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertTrue(restored.getVisitors().isEmpty());
        restarted.delete("A01");
        assertNull(restarted.find("A01"));
    }
}
