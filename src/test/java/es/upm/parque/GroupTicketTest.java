package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, TODAY);
        t.run("GROUP CREATE;G01;Familia Garcia");
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void discountBrackets(int members, String total) {
        assertEquals(new BigDecimal(total), GroupTicket.totalFor(new BigDecimal("40.00"), members));
    }

    @Test
    void purchaseGeneratesOneTicketPerMember() {
        t.addMembers("G01", 3);
        assertEquals("OK GROUP TICKET CREATED -> 108.00", t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        GroupTicket gt = assertInstanceOf(GroupTicket.class, t.tickets.get("TG01"));
        assertEquals(3, gt.getTickets().size());
        assertSame(t.groups.get("G01"), gt.getGroup());
        for (IndividualTicket internal : gt.getTickets()) {
            assertSame(gt, internal.getGroupTicket());
            assertEquals(new BigDecimal("40.00"), internal.getPrice());
            assertSame(internal, t.tickets.get(internal.getId()));
        }
        assertEquals(t.groups.get("G01").getMembers(), gt.getUsers());
    }

    @Test
    void rejectsEmptyOrUnknownGroupAndDuplicates() {
        assertTrue(t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15").startsWith("ERROR"));
        assertTrue(t.run("GROUPTICKET CREATE;TG01;G99;2026-10-15").startsWith("ERROR"));
        t.addMembers("G01", 1);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(t.run("GROUPTICKET CREATE;TG01;G01;2026-10-16").startsWith("ERROR"));
        assertTrue(t.run("GROUPTICKET CREATE;TG02;G01;2026-09-30").startsWith("ERROR"));
    }

    @Test
    void internalIdsSkipUsedIdentifiers() {
        t.addMembers("G01", 2);
        t.run("TICKET CREATE;TG01-1;u0@correo.com;2026-10-10", "GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket gt = assertInstanceOf(GroupTicket.class, t.tickets.get("TG01"));
        assertEquals("TG01-2", gt.getTickets().get(0).getId());
        assertEquals("TG01-3", gt.getTickets().get(1).getId());
    }

    @Test
    void lockedGroupCannotChangeUntilTicketIsPast() {
        t.addMembers("G01", 2);
        t.run(ParkTestSupport.CREATE_ANA, "GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-15");
        assertTrue(t.run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP REMOVE;G01;u0@correo.com").startsWith("ERROR"));
        assertTrue(t.run("GROUP DELETE;G01").startsWith("ERROR"));
        assertEquals(2, t.groups.get("G01").getMembers().size());

        t.run("SYSTEM DATE;2026-10-16");
        assertEquals("OK GROUP MEMBER REMOVED", t.run("GROUP REMOVE;G01;u0@correo.com"));
        GroupTicket gt = assertInstanceOf(GroupTicket.class, t.tickets.get("TG01"));
        assertEquals(2, gt.getTickets().size());
        assertEquals("OK GROUP DELETED", t.run("GROUP DELETE;G01"));
    }

    @Test
    void internalTicketsBlockMemberDeletion() {
        t.run(ParkTestSupport.CREATE_ANA, "GROUP ADD;G01;ana@correo.com", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "SYSTEM DATE;2026-10-10");
        assertTrue(t.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-16");
        assertEquals("OK USER DELETED", t.run("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void deleteOnlyFutureGroupTicketsWithTheirInternalTickets() {
        t.addMembers("G01", 3);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-15");
        assertTrue(t.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(t.run("TICKET DELETE;TG01-1").contains("GROUPTICKET DELETE"));
        assertTrue(t.run("TICKET DELETE;TG01").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-14");
        assertEquals("OK GROUP TICKET DELETED", t.run("GROUPTICKET DELETE;TG01"));
        assertTrue(t.tickets.getTickets().isEmpty());
        assertEquals("OK GROUP MEMBER REMOVED", t.run("GROUP REMOVE;G01;u0@correo.com"));
    }

    @Test
    void groupTicketDeleteRejectsIndividualTickets() {
        t.run(ParkTestSupport.CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(t.run("GROUPTICKET DELETE;T001").startsWith("ERROR"));
    }

    @Test
    void groupTicketsAreRestoredWithTheirTicketsAndLock() {
        t.addMembers("G01", 3);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ParkTestSupport restarted = t.restart(LocalDate.of(2026, 10, 15));
        GroupTicket gt = assertInstanceOf(GroupTicket.class, restarted.tickets.get("TG01"));
        assertEquals(new BigDecimal("108.00"), gt.getPrice());
        assertEquals(3, gt.getTickets().size());
        assertSame(restarted.groups.get("G01"), gt.getGroup());
        assertSame(restarted.users.findByEmail("u0@correo.com"), gt.getTickets().get(0).getUser());
        assertTrue(restarted.run("GROUP DELETE;G01").startsWith("ERROR"));
    }

    @Test
    void historyOfDeletedGroupIsKept() {
        t.addMembers("G01", 1);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-16", "GROUP DELETE;G01");
        GroupTicket gt = assertInstanceOf(GroupTicket.class, t.restart(TODAY).tickets.get("TG01"));
        assertEquals("G01", gt.getGroupId());
        assertEquals(1, gt.getTickets().size());
    }
}
