package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.IndividualTicket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CascadePersistenceTest extends ParkTestSupport {

    @BeforeEach
    void ticketsWithPasses() {
        run(ANA);
        run(PEDRO);
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SPEEDPASS CREATE;T001;10");
        run("SPECIALPASS CREATE;T001");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPEEDPASS CREATE;TG01-2;10");
        run("SPECIALPASS CREATE;TG01-2");
    }

    private String file(String name) throws IOException {
        Path path = dir.resolve(name);
        return Files.exists(path) ? Files.readString(path) : "";
    }

    @Test
    void ticketDeletionCascadesToItsPasses() throws IOException {
        assertTrue(ok(run("TICKET DELETE;T001")));
        assertFalse(file("passes.txt").contains("T001-"));
        assertFalse(file("tickets.txt").contains("T001;"));
        assertTrue(file("passes.txt").contains("TG01-2-SPECIAL"));

        restart();
        assertNull(park.getTickets().find("T001"));
        assertNull(park.getTickets().findPass("T001-SPEED1"));
        assertNull(park.getTickets().findPass("T001-SPECIAL"));
        assertNotNull(park.getTickets().findPass("TG01-1-SPEED1"));
    }

    @Test
    void groupTicketDeletionRemovesPassesConstituentsAndGroupTicket() throws IOException {
        assertTrue(ok(run("GROUPTICKET DELETE;TG01")));
        restart();
        assertNull(park.getTickets().find("TG01"));
        assertNull(park.getTickets().find("TG01-1"));
        assertNull(park.getTickets().find("TG01-2"));
        assertNull(park.getTickets().findPass("TG01-2-SPECIAL"));
        assertFalse(file("passes.txt").contains("TG01"));
        assertFalse(file("grouptickets.txt").contains("TG01"));
        assertEquals(11, ((IndividualTicket) park.getTickets().find("T001")).getPasses().size());
    }

    @Test
    void failedPreconditionDeletesNothing() throws IOException {
        run("SYSTEM DATE;2026-10-15");
        String passes = file("passes.txt");
        assertTrue(error(run("TICKET DELETE;T001")));
        assertTrue(error(run("GROUPTICKET DELETE;TG01")));
        assertEquals(passes, file("passes.txt"));
        assertEquals(11, ((IndividualTicket) park.getTickets().find("T001")).getPasses().size());
        assertNotNull(park.getTickets().find("TG01-2"));
    }

    @Test
    void failedSaveLeavesFilesAndMemoryUnchanged() throws IOException {
        String tickets = file("tickets.txt");
        String passes = file("passes.txt");
        String groupTickets = file("grouptickets.txt");
        Files.createDirectory(dir.resolve("grouptickets.txt.tmp"));

        assertTrue(error(run("GROUPTICKET DELETE;TG01")));
        assertTrue(error(run("TICKET DELETE;T001")));

        assertEquals(tickets, file("tickets.txt"));
        assertEquals(passes, file("passes.txt"));
        assertEquals(groupTickets, file("grouptickets.txt"));
        assertFalse(Files.exists(dir.resolve("passes.txt.tmp")));
        assertFalse(Files.exists(dir.resolve("tickets.txt.tmp")));
        assertNotNull(park.getTickets().find("TG01"));
        assertNotNull(park.getTickets().find("T001"));
        assertEquals(11, ((IndividualTicket) park.getTickets().find("T001")).getPasses().size());
    }
}
