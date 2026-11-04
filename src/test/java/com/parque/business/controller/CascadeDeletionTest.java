package com.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parque.ApplicationContext;
import com.parque.TestPark;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {
    @TempDir
    Path dir;
    ParkController c;

    @BeforeEach
    void setUp() throws Exception {
        c = TestPark.start(dir);
        c.setSystemDate(LocalDate.of(2026, 10, 1));
        c.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        c.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        c.createGroup("G01", "Familia");
        c.addGroupMember("G01", "ana@correo.com");
        c.addGroupMember("G01", "pedro@correo.com");
        c.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        c.createTicket("T002", "ana@correo.com", LocalDate.of(2026, 10, 1));
        c.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 15));
        c.buySpeedPasses("P02", "T001", "10");
        c.buySpecialPass("S01", "T001");
        c.buySpeedPasses("P05", "TG01-1", "1");
        c.buySpecialPass("S05", "TG01-2");
        c.buySpecialPass("S06", "T002");
    }

    @AfterEach
    void tearDown() {
        ApplicationContext.stop();
    }

    private List<String> passIds() throws Exception {
        return Files.readAllLines(dir.resolve("passes.txt")).stream().map(l -> l.split(";")[1]).toList();
    }

    @Test
    void individualCascadeSurvivesRestart() throws Exception {
        c.deleteTicket("T001");
        assertEquals(List.of("P05-1", "S05", "S06"), passIds());
        ApplicationContext.stop();
        ParkController again = TestPark.start(dir);
        assertThrows(IllegalArgumentException.class, () -> again.deleteTicket("T001"));
        assertEquals(List.of("T002", "TG01-1", "TG01-2"),
                Files.readAllLines(dir.resolve("tickets.txt")).stream().map(l -> l.split(";")[0]).toList());
    }

    @Test
    void groupCascadeRemovesPassesConstituentsAndGroupTicket() throws Exception {
        c.deleteGroupTicket("TG01");
        assertEquals(12, passIds().size());
        assertEquals(List.of("T001", "T002"),
                Files.readAllLines(dir.resolve("tickets.txt")).stream().map(l -> l.split(";")[0]).toList());
        assertEquals(List.of(), Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void failedValidationDeletesNothing() throws Exception {
        List<String> before = passIds();
        assertThrows(IllegalArgumentException.class, () -> c.deleteTicket("T002"), "dated today");
        assertThrows(IllegalArgumentException.class, () -> c.deleteTicket("TG01-1"), "constituent");
        assertThrows(IllegalArgumentException.class, () -> c.deleteTicket("T999"));
        c.setSystemDate(LocalDate.of(2026, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> c.deleteGroupTicket("TG01"));
        assertEquals(before, passIds());
    }

    @Test
    void failedWriteLeavesNoPartialCascade() throws Exception {
        List<String> passesBefore = Files.readAllLines(dir.resolve("passes.txt"));
        Path ticketsFile = dir.resolve("tickets.txt");
        List<String> ticketsBefore = Files.readAllLines(ticketsFile);
        Files.delete(ticketsFile);
        Files.createDirectory(ticketsFile); // the ticket file can no longer be written
        assertThrows(IllegalStateException.class, () -> c.deleteTicket("T001"));
        assertThrows(IllegalStateException.class, () -> c.deleteGroupTicket("TG01"));
        assertEquals(passesBefore, Files.readAllLines(dir.resolve("passes.txt")), "passes restored");
        Files.delete(ticketsFile);
        Files.write(ticketsFile, ticketsBefore);
        ApplicationContext.stop();
        ParkController again = TestPark.start(dir);
        again.setSystemDate(LocalDate.of(2026, 10, 1));
        assertEquals(14, passIds().size(), "ticket and passes intact after the failed attempts");
        again.deleteTicket("T001");
        assertEquals(List.of("P05-1", "S05", "S06"), passIds());
    }
}
