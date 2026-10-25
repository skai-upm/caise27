package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.Park;
import es.upm.parque.persistence.TextFileAttractionRepository;

class AttractionManagerTest {

    static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private Park park;
    private AttractionManager attractions;

    @BeforeEach
    void setUp() {
        park = new Park(dir, LocalDate.of(2026, 10, 15));
        attractions = park.attractions;
        park.users.create(Fixtures.ana());
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.groups.create("G01", "Grupo");
        for (int i = 0; i < 4; i++) {
            park.users.create(Fixtures.numbered(i));
        }
        for (int i = 0; i < 3; i++) {
            park.groups.addMember("G01", "u" + i + "@correo.com");
        }
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.groups.create("G02", "Cuatro");
        for (int i = 0; i < 4; i++) {
            park.groups.addMember("G02", "u" + i + "@correo.com");
        }
    }

    @Test
    void createsAndValidatesAttractions() {
        assertEquals(AttractionManager.ATTRACTION_CREATED, attractions.create("A01", "Tornado", "20"));
        assertEquals(20, attractions.find("A01").getCapacity());
        assertTrue(attractions.find("A01").isEmpty());
        assertEquals(AttractionManager.INVALID_CAPACITY, attractions.create("A02", "Noria", "0"));
        assertEquals(AttractionManager.INVALID_CAPACITY, attractions.create("A02", "Noria", "muchos"));
        assertEquals(AttractionManager.DUPLICATE_ATTRACTION, attractions.create("A01", "Otra", "5"));
        assertEquals(UserManager.EMPTY_FIELD, attractions.create("A02", "", "5"));
    }

    @Test
    void individualAndGroupTicketsEnterUniformly() {
        attractions.create("A01", "Tornado", "20");
        assertEquals("OK ENTERED 1 USERS", attractions.enter("A01", "T001"));
        assertEquals("OK ENTERED 3 USERS", attractions.enter("A01", "TG01"));
        assertEquals(4, attractions.find("A01").getPresent().size());
        assertEquals(16, attractions.find("A01").available());
    }

    @Test
    void rejectsInvalidEntries() {
        attractions.create("A01", "Tornado", "20");
        attractions.create("A02", "Noria", "20");
        park.tickets.create("T002", "ana@correo.com", "2026-10-16");
        assertEquals(AttractionManager.ATTRACTION_NOT_FOUND, attractions.enter("A99", "T001"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, attractions.enter("A01", "T999"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, attractions.enter("A01", "TG01-1"));
        assertEquals(AttractionManager.NOT_VALID_TODAY, attractions.enter("A01", "T002"));
        attractions.enter("A01", "T001");
        assertEquals(AttractionManager.ALREADY_INSIDE, attractions.enter("A02", "T001"));
        assertEquals(1, attractions.find("A01").getPresent().size());
    }

    @Test
    void groupEntryIsAtomicWhenCapacityIsShort() {
        park.date.set(LocalDate.of(2026, 10, 1));
        park.tickets.createGroupTicket("TG02", "G02", "2026-10-15");
        park.date.set(LocalDate.of(2026, 10, 15));
        attractions.create("A01", "Tornado", "4");
        attractions.enter("A01", "T001");
        assertEquals(3, attractions.find("A01").available());
        assertEquals(AttractionManager.CAPACITY_EXCEEDED, attractions.enter("A01", "TG02"));
        assertEquals(1, attractions.find("A01").getPresent().size());
        assertEquals("OK ENTERED 3 USERS", attractions.enter("A01", "TG01"));
    }

    @Test
    void exitReleasesCapacityWithoutDateCheck() {
        attractions.create("A01", "Tornado", "20");
        attractions.enter("A01", "T001");
        attractions.enter("A01", "TG01");
        park.date.set(LocalDate.of(2026, 10, 16));
        assertEquals("OK EXITED 1 USERS", attractions.exit("A01", "T001"));
        assertEquals(AttractionManager.NOT_INSIDE, attractions.exit("A01", "T001"));
        assertEquals(AttractionManager.ATTRACTION_NOT_FOUND, attractions.exit("A99", "T001"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, attractions.exit("A01", "T999"));
        assertEquals(17, attractions.find("A01").available());
    }

    @Test
    void statusReflectsCurrentState() {
        attractions.create("A01", "Tornado", "20");
        assertEquals(String.join(NL, "ATTRACTION A01 - Tornado", "Capacity: 20 | Present: 0 | Available: 20",
                "Users present:", "- none"), attractions.status("A01"));
        attractions.enter("A01", "T001");
        attractions.enter("A01", "TG01");
        attractions.exit("A01", "T001");
        assertEquals(String.join(NL, "ATTRACTION A01 - Tornado", "Capacity: 20 | Present: 3 | Available: 17",
                "Users present:", "- U0 Apellido (u0@correo.com)", "- U1 Apellido (u1@correo.com)",
                "- U2 Apellido (u2@correo.com)"), attractions.status("A01"));
        assertEquals(AttractionManager.ATTRACTION_NOT_FOUND, attractions.status("A99"));
    }

    @Test
    void deletesOnlyEmptyAttractions() {
        attractions.create("A01", "Tornado", "20");
        attractions.enter("A01", "T001");
        assertEquals(AttractionManager.NOT_EMPTY, attractions.delete("A01"));
        attractions.exit("A01", "T001");
        assertEquals(AttractionManager.ATTRACTION_DELETED, attractions.delete("A01"));
        assertEquals(AttractionManager.ATTRACTION_NOT_FOUND, attractions.delete("A01"));
        assertNull(attractions.find("A01"));
    }

    @Test
    void restartRestoresEmptyAttractions() throws IOException {
        attractions.create("A01", "Tornado", "20");
        attractions.enter("A01", "T001");
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        Park restarted = new Park(dir, LocalDate.of(2026, 10, 15));
        assertTrue(restarted.attractions.find("A01").isEmpty());
        assertEquals("OK ENTERED 1 USERS", restarted.attractions.enter("A01", "T001"));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = Files.createFile(dir.resolve("blocked"));
        AttractionManager broken = AttractionManager.initialize(new TextFileAttractionRepository(blocked.resolve("a.txt")),
                park.tickets, park.date);
        assertEquals(UserManager.STORAGE_FAILURE, broken.create("A01", "Tornado", "20"));
    }
}
