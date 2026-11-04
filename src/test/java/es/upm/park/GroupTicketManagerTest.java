package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    @TempDir
    Path dir;
    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserFileStore(dir.resolve("users.txt")));
        users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        groups = new GroupManager(new GroupFileStore(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.create("G02", "Vacio");
        tickets = reload();
    }

    private TicketManager reload() throws IOException {
        return new TicketManager(new TicketFileStore(dir.resolve("tickets.txt")),
                new GroupTicketFileStore(dir.resolve("grouptickets.txt")), users, groups);
    }

    private Group g01() {
        return groups.find("G01").orElseThrow();
    }

    private User ana() {
        return users.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void createComposedTicketWithVerbatimOutput() {
        Result r = tickets.createGroupTicket("TG01", g01(), OCT_15, OCT_1, P40);
        assertEquals("OK GROUP TICKET CREATED -> 80.00", r.toString());
        GroupTicket t = tickets.findGroupTicket("TG01").orElseThrow();
        assertEquals(2, t.getTickets().size());
        assertEquals(1, tickets.allTickets().size());
    }

    @Test
    void rejectedPurchasesCreateNothing() throws IOException {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertFalse(tickets.createGroupTicket("TG02", groups.find("G02").orElseThrow(), OCT_15, OCT_1, P40).success());
        assertFalse(tickets.createGroupTicket("T001", g01(), OCT_15, OCT_1, P40).success());
        assertFalse(tickets.createGroupTicket("TG01", g01(), OCT_1.minusDays(1), OCT_1, P40).success());
        assertFalse(tickets.createGroupTicket(" ", g01(), OCT_15, OCT_1, P40).success());
        assertTrue(tickets.getGroupTickets().isEmpty());
        assertFalse(Files.exists(dir.resolve("grouptickets.txt")));
    }

    @Test
    void constituentIdsAreReserved() {
        tickets.createGroupTicket("TG01", g01(), OCT_15, OCT_1, P40);
        assertFalse(tickets.create("TG01-12345678Z", "ana@correo.com", OCT_15, OCT_1, P40).success());
        assertFalse(tickets.create("TG01", "ana@correo.com", OCT_15, OCT_1, P40).success());
        Result r = tickets.delete("TG01-12345678Z", OCT_1);
        assertFalse(r.success());
        assertTrue(r.message().contains("group ticket"));
    }

    @Test
    void lockAndUserBlockFollowSystemDate() {
        tickets.createGroupTicket("TG01", g01(), OCT_15, OCT_1, P40);
        assertTrue(tickets.isGroupLocked(g01(), OCT_1));
        assertTrue(tickets.isGroupLocked(g01(), OCT_15));
        assertFalse(tickets.isGroupLocked(g01(), OCT_15.plusDays(1)));
        assertFalse(tickets.isGroupLocked(groups.find("G02").orElseThrow(), OCT_1));
        assertTrue(tickets.hasTicketOnOrAfter(ana(), OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(ana(), OCT_15.plusDays(1)));
    }

    @Test
    void deleteOnlyFutureGroupTicketsWithConstituents() throws IOException {
        tickets.createGroupTicket("TG001", g01(), OCT_15, OCT_1, P40);
        assertFalse(tickets.deleteGroupTicket("TG001", OCT_15).success());
        assertFalse(tickets.deleteGroupTicket("TG999", OCT_1).success());
        assertTrue(tickets.deleteGroupTicket("TG001", OCT_1).success());
        assertTrue(tickets.getGroupTickets().isEmpty());
        assertFalse(tickets.hasTicketOnOrAfter(ana(), OCT_1));
        assertTrue(Files.readAllLines(dir.resolve("grouptickets.txt")).isEmpty());
    }

    @Test
    void restartRestoresGroupTicketAndLock() throws IOException {
        tickets.createGroupTicket("TG01", g01(), OCT_15, OCT_1, P40);
        TicketManager again = reload();
        assertTrue(again.isGroupLocked(g01(), OCT_1));
        assertTrue(again.hasTicketOnOrAfter(ana(), OCT_1));
        assertEquals(List.of("TG01;2026-10-15;80.00;G01;40.00;12345678Z;87654321X"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void accessLookupAcceptsStandaloneAndGroupTicketsOnly() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        tickets.createGroupTicket("TG01", g01(), OCT_15, OCT_1, P40);
        assertEquals(List.of(ana()), tickets.findForAccess("T001").orElseThrow().getHolders());
        assertEquals(2, tickets.findForAccess("TG01").orElseThrow().getHolders().size());
        assertTrue(tickets.findForAccess("TG01-12345678Z").isEmpty());
        assertTrue(tickets.findForAccess("T999").isEmpty());
    }
}
