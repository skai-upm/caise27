package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import es.upm.amusementpark.business.model.GroupTicket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketCommandsTest extends ParkTestSupport {

    @ParameterizedTest
    @CsvSource({"2, 80.00", "3, 108.00", "5, 180.00", "6, 192.00", "10, 320.00", "11, 220.00"})
    void reportsDiscountedTotal(int members, String total) {
        createUsers(members);
        createGroupWithUsers("G01", members);
        expect("OK GROUP TICKET CREATED -> " + total, "GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket ticket = assertInstanceOf(GroupTicket.class, park.findTicket("TG01"));
        assertEquals(members, ticket.getTickets().size());
    }

    @Test
    void usesBasePriceAtPurchase() {
        createUsers(3);
        createGroupWithUsers("G01", 3);
        ok("SYSTEM PRICE;60.00");
        expect("OK GROUP TICKET CREATED -> 162.00", "GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    @Test
    void failedPurchasesCreateNothing() {
        createUsers(2);
        ok("GROUP CREATE;G02;Vacia");
        error("GROUPTICKET CREATE;TG02;G02;2026-10-15");
        error("GROUPTICKET CREATE;TG02;G99;2026-10-15");
        createGroupWithUsers("G01", 2);
        error("GROUPTICKET CREATE;TG01;G01;2026-09-30");
        ok("TICKET CREATE;TG01-2;" + email(1) + ";2026-10-15");
        error("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG02"));
        assertEquals(1, park.getTickets().size());
        error("TICKET CREATE;TG01-2;" + email(2) + ";2026-10-15");
    }

    @Test
    void groupTicketIdsShareNamespaceWithTickets() {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        error("TICKET CREATE;TG01;" + email(1) + ";2026-10-15");
        error("TICKET CREATE;TG01-1;" + email(1) + ";2026-10-15");
        error("GROUPTICKET CREATE;TG01;G01;2026-10-16");
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void deletesFutureGroupTicketWithConstituents() {
        createUsers(3);
        createGroupWithUsers("G01", 3);
        ok("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        error("TICKET DELETE;TG001");
        error("TICKET DELETE;TG001-1");
        expect("OK: group ticket TG001 deleted", "GROUPTICKET DELETE;TG001");
        assertTrue(park.getTickets().isEmpty());
        ok("TICKET CREATE;TG001-1;" + email(1) + ";2026-10-15");
    }

    @Test
    void todayOrPastGroupTicketIsKept() {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SYSTEM DATE;2026-10-15");
        error("GROUPTICKET DELETE;TG01");
        ok("SYSTEM DATE;2026-10-20");
        error("GROUPTICKET DELETE;TG01");
        GroupTicket ticket = assertInstanceOf(GroupTicket.class, park.findTicket("TG01"));
        assertEquals(2, ticket.getTickets().size());
        error("GROUPTICKET DELETE;UNKNOWN");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-25");
        error("GROUPTICKET DELETE;T001");
    }

    @Test
    void activeGroupTicketLocksGroupUntilItIsPast() {
        createUsers(3);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        error("GROUP ADD;G01;" + email(3));
        error("GROUP REMOVE;G01;" + email(1));
        error("GROUP DELETE;G01");
        ok("SYSTEM DATE;2026-10-15");
        error("GROUP DELETE;G01");
        ok("SYSTEM DATE;2026-10-16");
        ok("GROUP ADD;G01;" + email(3));
        ok("GROUP REMOVE;G01;" + email(1));
        GroupTicket ticket = (GroupTicket) park.findTicket("TG01");
        assertEquals(2, ticket.getTickets().size());
        ok("GROUP DELETE;G01");
    }

    @Test
    void lockIsNotStoredAsFlag() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        restart();
        error("GROUP ADD;G01;" + email(2));
        assertTrue(park.isLocked(park.findGroup("G01")));
        ok("SYSTEM DATE;2026-10-16");
        assertTrue(!park.isLocked(park.findGroup("G01")));
        ok("GROUP REMOVE;G01;" + email(2));
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void constituentTicketBlocksMemberDeletion() {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        error("USER DELETE;" + email(1) + ";Clave1!");
        ok("SYSTEM DATE;2026-10-16");
        ok("USER DELETE;" + email(1) + ";Clave1!");
        GroupTicket ticket = (GroupTicket) park.findTicket("TG01");
        assertEquals(dni(1), ticket.getTickets().get(0).getUser().getDni());
    }

    @Test
    void groupTicketSurvivesRestart() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        restart();
        GroupTicket ticket = assertInstanceOf(GroupTicket.class, park.findTicket("TG01"));
        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertEquals(List.of(park.getUserManager().findByEmail(email(1)), park.getUserManager().findByEmail(email(2))),
                ticket.getUsers());
        assertEquals(3, Files.readAllLines(tempDir.resolve("tickets.txt")).size());
    }

    @Test
    void historyKeptWhenGroupAndMembersAreDeleted() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SYSTEM DATE;2026-10-16");
        ok("GROUP DELETE;G01");
        ok("USER DELETE;" + email(1) + ";Clave1!");
        restart();
        GroupTicket ticket = assertInstanceOf(GroupTicket.class, park.findTicket("TG01"));
        assertNull(park.findGroup("G01"));
        assertEquals("G01", ticket.getGroup().getId());
        assertEquals("Group G01", ticket.getGroup().getName());
        assertEquals(email(1), ticket.getTickets().get(0).getUser().getEmail());
        assertNotNull(park.getUserManager().findByEmail(email(2)));
        assertSame(park.getUserManager().findByEmail(email(2)), ticket.getTickets().get(1).getUser());
    }
}
