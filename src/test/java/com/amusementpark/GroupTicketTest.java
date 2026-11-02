package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
    }

    private GroupTicket groupTicket(String id) {
        return (GroupTicket) f.park.getTicketManager().find(id);
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void appliesDiscountBrackets(int members, String expected) {
        f.group("G01", members, 10000000);
        assertEquals("OK GROUP TICKET CREATED -> " + expected, f.run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals(members, groupTicket("TG01").getTickets().size());
    }

    @Test
    void discountRoundsHalfUp() {
        assertEquals("89.99", TicketManager.groupPrice(new BigDecimal("33.33"), 3).toPlainString());
    }

    @Test
    void createsOneConstituentPerMemberAtPurchaseTime() {
        f.group("G01", 2, 20000000);
        f.ok("SYSTEM PRICE;50.00", "GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket gt = groupTicket("TG01");
        List<User> members = f.park.getGroupManager().find("G01").getMembers();
        assertEquals(List.of("TG01-1", "TG01-2"), gt.getTickets().stream().map(Ticket::getId).toList());
        assertSame(members.get(0), gt.getTickets().get(0).getUser());
        assertSame(members.get(1), gt.getTickets().get(1).getUser());
        assertEquals("50.00", gt.getTickets().get(0).getPrice().toPlainString());
        assertSame(f.park.getGroupManager().find("G01"), gt.getGroup());

        f.user("Late", 20000009);
        f.ok("SYSTEM DATE;2026-10-16", "GROUP ADD;G01;late@correo.com");
        assertEquals(2, groupTicket("TG01").getTickets().size());
    }

    @Test
    void failedPurchasesCreateNothing() {
        f.group("G01", 2, 30000000);
        f.ok("GROUP CREATE;EMPTY;Vacio", "TICKET CREATE;TG02-2;g01m0@correo.com;2026-10-15");
        assertTrue(f.run("GROUPTICKET CREATE;TG01;G99;2026-10-15").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET CREATE;TG01;EMPTY;2026-10-15").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET CREATE;TG01;G01;2026-09-30").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET CREATE;TG01;G01;2026-02-30").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET CREATE;TG02-2;G01;2026-10-15").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET CREATE;TG02;G01;2026-10-15").startsWith("ERROR:"), "constituent id clash");
        assertTrue(f.park.getTicketManager().getGroupTickets().isEmpty());
        assertEquals(1, f.park.getTicketManager().getTickets().size());
    }

    @Test
    void activeGroupTicketLocksGroupAndMembers() {
        f.group("G01", 2, 40000000);
        f.user("Other", 40000009);
        f.ok("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-15");
        assertTrue(f.run("GROUP ADD;G01;other@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP REMOVE;G01;g01m0@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("GROUP DELETE;G01").startsWith("ERROR:"));
        assertTrue(f.run("USER DELETE;g01m0@correo.com;Clave1!").startsWith("ERROR:"));
        assertEquals(2, f.park.getGroupManager().find("G01").getMembers().size());

        f.ok("SYSTEM DATE;2026-10-16", "GROUP ADD;G01;other@correo.com", "GROUP REMOVE;G01;other@correo.com",
                "USER DELETE;g01m0@correo.com;Clave1!", "GROUP DELETE;G01");
        assertNotNull(groupTicket("TG01"), "historical group ticket is kept");
    }

    @Test
    void ticketDeleteRejectsConstituents() {
        f.group("G01", 2, 50000000);
        f.ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(f.run("TICKET DELETE;TG01-1").startsWith("ERROR:"));
        assertEquals(2, f.park.getTicketManager().getTickets().size());
    }

    @Test
    void deletesFutureGroupTicketWithAllConstituents() {
        f.group("G01", 3, 60000000);
        f.ok("GROUPTICKET CREATE;TG01;G01;2026-10-15", "GROUPTICKET CREATE;TG02;G01;2026-10-20",
                "SYSTEM DATE;2026-10-15");
        assertTrue(f.run("GROUPTICKET DELETE;TG01").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET DELETE;NOPE").startsWith("ERROR:"));
        assertTrue(f.run("GROUPTICKET DELETE;T001").startsWith("ERROR:"));
        assertEquals(6, f.park.getTicketManager().getTickets().size());

        assertEquals("OK: Group ticket deleted: TG02 (3 tickets)", f.run("GROUPTICKET DELETE;TG02"));
        assertNull(groupTicket("TG02"));
        assertNull(f.park.getTicketManager().find("TG02-1"));
        assertEquals(3, f.park.getTicketManager().getTickets().size());
        assertNotNull(groupTicket("TG01"));
    }

    @Test
    void restoresCompositionAndHistoryOnRestart() throws IOException {
        f.group("G01", 2, 70000000);
        f.ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        f.restart();
        GroupTicket gt = groupTicket("TG01");
        assertEquals("80.00", gt.getPrice().toPlainString());
        assertSame(f.park.getGroupManager().find("G01"), gt.getGroup());
        assertEquals(2, gt.getTickets().size());
        assertSame(f.park.getUserManager().findByEmail("g01m0@correo.com"), gt.getTickets().get(0).getUser());
        assertSame(gt.getTickets().get(0), f.park.getTicketManager().find("TG01-1"));

        f.ok("SYSTEM DATE;2026-10-16", "GROUP DELETE;G01");
        f.restart();
        gt = groupTicket("TG01");
        assertEquals("G01", gt.getGroup().getId());
        assertEquals("Grupo G01", gt.getGroup().getName());
        assertNull(f.park.getGroupManager().find("G01"));
    }
}
