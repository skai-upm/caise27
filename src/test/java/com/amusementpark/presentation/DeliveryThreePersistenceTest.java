package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.controller.GroupController;
import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.controller.UserController;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.SpeedPass;
import com.amusementpark.business.model.User;
import com.amusementpark.persistence.StoragePaths;
import com.amusementpark.persistence.TicketFileRepository;
import com.amusementpark.persistence.TicketRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryThreePersistenceTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.group("G01", 2, 60000000);
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15", "GROUPTICKET CREATE;TG01;G01;2026-10-20");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().find(id);
    }

    @Test
    void passesAndStatusSurviveRestart() throws IOException {
        f.ok("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001", "SPEEDPASS CREATE;TG01-1;1",
                "ATTRACTION CREATE;A01;Tornado;20", "SYSTEM DATE;2026-10-15", "ATTRACTION ENTER;A01;T001;SPEED");
        f.restart();
        IndividualTicket t = ticket("T001");
        List<SpeedPass> speed = t.getSpeedPasses();
        assertEquals(10, speed.size());
        assertTrue(speed.get(0).isConsumed());
        assertFalse(speed.get(1).isConsumed());
        assertEquals("8.00", speed.get(9).getPrice().toPlainString());
        assertEquals("T001-SPECIAL", t.getSpecialPass().getId());
        assertEquals("40.00", t.getSpecialPass().getPrice().toPlainString());
        assertEquals(1, ticket("TG01-1").getSpeedPasses().size());
        assertEquals("OK SPEEDPASS CREATED -> 8.00", f.run("SPEEDPASS CREATE;T001;1"));
        assertEquals("T001-S11", ticket("T001").getSpeedPasses().get(10).getId());
    }

    @Test
    void specialPassHistorySurvivesRefundAndRestart() throws IOException {
        f.ok("SPECIALPASS CREATE;T001", "SPECIALPASS DELETE;T001-SPECIAL");
        f.restart();
        assertNull(ticket("T001").getSpecialPass());
        assertTrue(f.run("SPECIALPASS CREATE;T001").startsWith("ERROR:"));
    }

    @Test
    void attractionTypeSurvivesRestart() throws IOException {
        f.ok("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION CREATE;A04;Magica;5;SPECIAL");
        f.restart();
        assertEquals(AttractionType.GENERAL, AttractionController.getInstance().find("A01").getType());
        assertEquals(AttractionType.SPECIAL, AttractionController.getInstance().find("A04").getType());
    }

    @Test
    void e2FilesWithoutNewColumnsStillLoad() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A09;Antigua;7"));
        Files.write(dir.resolve("tickets.txt"),
                List.of("T009;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Apellido;"));
        Files.deleteIfExists(dir.resolve("group_tickets.txt"));
        Files.deleteIfExists(dir.resolve("passes.txt"));
        f.restart();
        assertEquals(AttractionType.GENERAL, AttractionController.getInstance().find("A09").getType());
        assertEquals(7, AttractionController.getInstance().find("A09").getCapacity());
        assertNotNull(ticket("T009"));
        f.ok("SPECIALPASS CREATE;T009");
    }

    @Test
    void ticketDeletionCascadesToPersistedPasses() throws IOException {
        f.ok("TICKET CREATE;T002;ana@correo.com;2026-10-20", "SPEEDPASS CREATE;T002;10", "SPECIALPASS CREATE;T002",
                "SPEEDPASS CREATE;T001;1", "TICKET DELETE;T002");
        f.restart();
        assertNull(TicketController.getInstance().find("T002"));
        String passes = Files.readString(dir.resolve("passes.txt"));
        assertFalse(passes.contains("T002"));
        assertTrue(passes.contains("T001-S1"));
    }

    @Test
    void groupTicketDeletionCascadesToConstituentsAndPasses() throws IOException {
        f.ok("SPEEDPASS CREATE;TG01-1;10", "SPECIALPASS CREATE;TG01-2", "GROUPTICKET DELETE;TG01");
        f.restart();
        assertNull(TicketController.getInstance().find("TG01"));
        assertNull(TicketController.getInstance().find("TG01-1"));
        assertFalse(Files.readString(dir.resolve("passes.txt")).contains("TG01"));
        assertFalse(Files.readString(dir.resolve("tickets.txt")).contains("TG01"));
        assertEquals("", Files.readString(dir.resolve("group_tickets.txt")).trim());
    }

    @Test
    void rejectedDeletionLeavesEverything() throws IOException {
        f.ok("SPEEDPASS CREATE;TG01-1;1", "SYSTEM DATE;2026-10-20");
        String before = Files.readString(dir.resolve("passes.txt")) + Files.readString(dir.resolve("tickets.txt"));
        assertTrue(f.run("GROUPTICKET DELETE;TG01").startsWith("ERROR:"));
        assertEquals(before, Files.readString(dir.resolve("passes.txt")) + Files.readString(dir.resolve("tickets.txt")));
        assertEquals(1, ticket("TG01-1").getSpeedPasses().size());
    }

    @Test
    void failedSaveLeavesNoPartialDeletion() throws IOException {
        f.ok("SPEEDPASS CREATE;TG01-1;1");
        StoragePaths paths = StoragePaths.in(dir);
        TicketRepository failing = new TicketRepository() {
            private final TicketFileRepository real =
                    new TicketFileRepository(paths.tickets(), paths.groupTickets(), paths.passes());

            @Override
            public Loaded load(List<User> users, List<Group> groups) throws IOException {
                return real.load(users, groups);
            }

            @Override
            public void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException {
                throw new IOException("disk full");
            }
        };
        TicketController.getInstance().configure(failing, UserController.getInstance(), GroupController.getInstance());
        assertTrue(f.run("GROUPTICKET DELETE;TG01").startsWith("ERROR:"));
        assertNotNull(TicketController.getInstance().find("TG01"));
        assertEquals(1, ticket("TG01-1").getSpeedPasses().size());
        assertTrue(f.run("TICKET DELETE;T001").startsWith("ERROR:"));
        assertNotNull(TicketController.getInstance().find("T001"));
    }
}
