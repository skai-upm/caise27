package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;

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

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run("SYSTEM DATE;2026-10-01");
    }

    private TicketController tickets() {
        return s.park().getTicketController();
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void discountBrackets(int members, String total) {
        s.createGroup("G01", members, 1);
        assertEquals("OK GROUP TICKET CREATED -> " + total, s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
    }

    @Test
    void discountRates() {
        assertEquals(0, GroupTicket.discountFor(2).signum());
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(3));
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(5));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(6));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(10));
        assertEquals(new BigDecimal("0.50"), GroupTicket.discountFor(11));
    }

    @Test
    void oneConstituentPerMemberReferencingUsers() {
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket gt = (GroupTicket) tickets().findById("TG01");
        Group group = s.park().getGroupController().findById("G01");
        assertSame(group, gt.getGroup());
        assertEquals(List.of("TG01-1", "TG01-2", "TG01-3"),
                gt.getTickets().stream().map(Ticket::getId).toList());
        assertEquals(group.getMembers(), gt.getUsers());
        assertEquals(new BigDecimal("36.00"), gt.getTickets().get(0).getPrice());
        assertSame(gt.getTickets().get(1), tickets().findById("TG01-2"));
    }

    @Test
    void failedPurchasesCreateNothing() {
        s.createGroup("G01", 3, 1);
        s.run("GROUP CREATE;GEMPTY;Vacio", "TICKET CREATE;TG02-1;user1@correo.com;2026-10-15");
        assertTrue(s.run("GROUPTICKET CREATE;TG01;G99;2026-10-15").startsWith("ERROR"));
        assertTrue(s.run("GROUPTICKET CREATE;TG01;GEMPTY;2026-10-15").startsWith("ERROR"));
        assertTrue(s.run("GROUPTICKET CREATE;TG01;G01;2026-09-30").startsWith("ERROR"));
        assertTrue(s.run("GROUPTICKET CREATE;TG01;G01;2026-10-xx").startsWith("ERROR"));
        assertTrue(s.run("GROUPTICKET CREATE;TG02-1;G01;2026-10-15").startsWith("ERROR"));
        assertTrue(s.run("GROUPTICKET CREATE;TG02;G01;2026-10-15").startsWith("ERROR"));
        assertTrue(tickets().getGroupTickets().isEmpty());
        assertNull(tickets().findById("TG02-2"));
    }

    @Test
    void compositionFixedAtPurchase() {
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-16");
        assertTrue(s.run("GROUP ADD;G01;" + s.createUser(9)).startsWith("OK"));
        assertEquals(3, ((GroupTicket) tickets().findById("TG01")).getTickets().size());
    }

    @Test
    void deletionIsAllOrNothingAndFutureOnly() {
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-15");
        assertTrue(s.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertNotNull(tickets().findById("TG01-3"));
        assertTrue(s.run("TICKET DELETE;TG01-1").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-14");
        assertTrue(s.run("GROUPTICKET DELETE;TG01").startsWith("OK"));
        assertNull(tickets().findById("TG01"));
        assertNull(tickets().findById("TG01-1"));
        assertNull(tickets().findById("TG01-3"));
        assertTrue(s.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
    }

    @Test
    void lockedGroupAndMembers() {
        s.createGroup("G01", 3, 1);
        s.createUser(9);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(s.run("GROUP ADD;G01;user9@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP REMOVE;G01;user1@correo.com").startsWith("ERROR"));
        assertTrue(s.run("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(s.run("USER DELETE;user1@correo.com;Clave1!").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-15");
        assertTrue(s.run("GROUP REMOVE;G01;user1@correo.com").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-16");
        assertTrue(s.run("GROUP REMOVE;G01;user1@correo.com").startsWith("OK"));
        assertTrue(s.run("USER DELETE;user1@correo.com;Clave1!").startsWith("OK"));
        assertTrue(s.run("GROUP DELETE;G01").startsWith("OK"));
        assertNotNull(tickets().findById("TG01"));
    }

    @Test
    void restartRestoresGroupTickets() throws IOException {
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        s.restart();
        GroupTicket gt = (GroupTicket) tickets().findById("TG01");
        assertSame(s.park().getGroupController().findById("G01"), gt.getGroup());
        assertEquals(new BigDecimal("108.00"), gt.getPrice());
        assertEquals("2026-10-15", gt.getDate().toString());
        assertEquals(3, gt.getTickets().size());
        assertSame(s.park().getUserController().findByEmail("user2@correo.com"), gt.getTickets().get(1).getUser());
    }

    @Test
    void historicalGroupTicketSurvivesGroupAndUserDeletion() throws IOException {
        s.createGroup("G01", 2, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-20", "GROUP DELETE;G01",
                "USER DELETE;user1@correo.com;Clave1!");
        s.restart();
        GroupTicket gt = (GroupTicket) tickets().findById("TG01");
        assertEquals("G01", gt.getGroup().getId());
        assertEquals("user1@correo.com", gt.getTickets().get(0).getUser().getEmail());
        assertNull(s.park().getGroupController().findById("G01"));
    }
}
