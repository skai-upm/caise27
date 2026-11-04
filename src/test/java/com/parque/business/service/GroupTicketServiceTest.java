package com.parque.business.service;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketServiceTest {
    static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    static final BigDecimal P40 = new BigDecimal("40.00");

    @TempDir
    Path dir;
    UserService users;
    GroupService groups;
    TicketService tickets;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        users.create("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "luis@correo.com");
        groups.create("G02", "Vacio");
        tickets = newService();
    }

    private TicketService newService() throws Exception {
        return new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
    }

    private List<String> read(String name) throws Exception {
        Path f = dir.resolve(name);
        return Files.exists(f) ? Files.readAllLines(f) : List.of();
    }

    @Test
    void oneConstituentPerMemberReferencingUsers() throws Exception {
        GroupTicket gt = tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        assertEquals("108.00", gt.getPriceText());
        assertEquals(3, gt.getConstituents().size());
        assertSame(users.findByEmail("ana@correo.com"), gt.getConstituents().get(0).getUser());
        assertEquals("TG01-3", gt.getConstituents().get(2).getId());
        assertSame(groups.find("G01"), gt.getGroup());
        assertEquals(List.of("TG01;G01;2026-10-15;40.00;108.00;TG01-1,TG01-2,TG01-3"), read("grouptickets.txt"));
        assertEquals(List.of("TG01-1;12345678Z;2026-10-15;36.00", "TG01-2;87654321X;2026-10-15;36.00",
                "TG01-3;11111111H;2026-10-15;36.00"), read("tickets.txt"));
    }

    @Test
    void rejectedPurchasesCreateNothing() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        tickets.create("TX-2", "ana@correo.com", OCT_15, OCT_1, P40);
        List<String> before = read("tickets.txt");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G99", OCT_15, OCT_1, P40));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G02", OCT_15, OCT_1, P40));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.createGroupTicket("TG01", "G01", LocalDate.of(2026, 9, 30), OCT_1, P40));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("T001", "G01", OCT_15, OCT_1, P40));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TX", "G01", OCT_15, OCT_1, P40),
                "generated id TX-2 already used");
        assertEquals(before, read("tickets.txt"));
        assertEquals(List.of(), read("grouptickets.txt"));
        assertEquals(0, tickets.getGroupTickets().size());
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        assertThrows(IllegalArgumentException.class, () -> tickets.create("TG01", "ana@correo.com", OCT_15, OCT_1, P40));
    }

    @Test
    void compositionFrozenAtPurchase() {
        GroupTicket gt = tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        groups.removeMember("G01", "luis@correo.com");
        assertEquals(3, gt.getConstituents().size());
    }

    @Test
    void lockAndUserBlockingFollowTheDate() {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        Group g = groups.find("G01");
        User ana = users.findByEmail("ana@correo.com");
        assertTrue(tickets.isGroupLocked(g, OCT_1));
        assertTrue(tickets.isGroupLocked(g, OCT_15));
        assertFalse(tickets.isGroupLocked(g, LocalDate.of(2026, 10, 16)));
        assertTrue(tickets.hasCurrentOrFutureTicket(ana, OCT_15));
        assertFalse(tickets.hasCurrentOrFutureTicket(ana, LocalDate.of(2026, 10, 16)));
        assertFalse(tickets.isGroupLocked(groups.find("G02"), OCT_1));
    }

    @Test
    void deleteFutureGroupTicketWithConstituents() throws Exception {
        tickets.createGroupTicket("TG001", "G01", OCT_15, OCT_1, P40);
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("TG001-1", OCT_1), "constituent alone");
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG001", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG999", OCT_1));
        tickets.deleteGroupTicket("TG001", LocalDate.of(2026, 10, 14));
        assertNull(tickets.findGroupTicket("TG001"));
        assertNull(tickets.find("TG001-1"));
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"), read("tickets.txt"));
        assertEquals(List.of(), read("grouptickets.txt"));
    }

    @Test
    void restartRestoresCompositionAndLock() throws Exception {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        TicketService restarted = newService();
        GroupTicket gt = restarted.findGroupTicket("TG01");
        assertSame(groups.find("G01"), gt.getGroup());
        assertEquals(3, gt.getConstituents().size());
        assertSame(restarted.find("TG01-2"), gt.getConstituents().get(1));
        assertSame(users.findByEmail("pedro@correo.com"), gt.getConstituents().get(1).getUser());
        assertTrue(restarted.isGroupLocked(groups.find("G01"), OCT_15));
        assertEquals(new BigDecimal("108.00"), gt.getPrice());
    }

    @Test
    void deletedGroupKeptAsHistoryId() throws Exception {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, P40);
        groups.delete("G01");
        TicketService restarted = newService();
        GroupTicket gt = restarted.findGroupTicket("TG01");
        assertNull(gt.getGroup());
        assertEquals("G01", gt.getGroupId());
    }
}
