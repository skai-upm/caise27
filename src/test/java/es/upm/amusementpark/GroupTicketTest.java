package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest extends ParkTestSupport {

    /** Creates group G01 with {@code size} new members. */
    private void groupOf(int size) {
        run("GROUP CREATE;G01;Grupo");
        for (int i = 1; i <= size; i++) {
            createUser(i);
            run("GROUP ADD;G01;u" + i + "@correo.com");
        }
    }

    private GroupTicket groupTicket(String id) {
        return (GroupTicket) park.getTickets().find(id);
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void appliesDiscountBrackets(int members, String total) {
        groupOf(members);
        assertEquals("OK GROUP TICKET CREATED -> " + total, run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
    }

    @Test
    void discountUsesCurrentBasePrice() {
        groupOf(3);
        run("SYSTEM PRICE;60.00");
        assertEquals("OK GROUP TICKET CREATED -> 162.00", run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
    }

    @Test
    void createsOneConstituentPerMember() {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        GroupTicket tg = groupTicket("TG01");
        assertSame(park.getGroups().find("G01"), tg.getGroup());
        assertEquals(List.of("TG01-1", "TG01-2"), tg.getTickets().stream().map(Ticket::getId).toList());
        assertSame(park.getUsers().findByEmail("u1@correo.com"), tg.getTickets().get(0).getUser());
        assertEquals(new BigDecimal("40.00"), tg.getTickets().get(1).getPrice());
    }

    @Test
    void compositionIsCapturedAtPurchase() {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SYSTEM DATE;2026-10-16");
        createUser(3);
        assertTrue(ok(run("GROUP ADD;G01;u3@correo.com")));
        assertEquals(2, groupTicket("TG01").getTickets().size());
    }

    @Test
    void failedPurchaseCreatesNothing() {
        groupOf(2);
        run("GROUP CREATE;G02;Vacio");
        run("TICKET CREATE;TG02-2;u1@correo.com;2026-10-15");
        assertTrue(error(run("GROUPTICKET CREATE;TG01;G99;2026-10-15")));
        assertTrue(error(run("GROUPTICKET CREATE;TG01;G02;2026-10-15")));
        assertTrue(error(run("GROUPTICKET CREATE;TG02;G01;2026-10-15")));
        assertTrue(error(run("GROUPTICKET CREATE;TG03;G01;2026-09-01")));
        assertTrue(park.getTickets().getGroupTickets().isEmpty());
        assertNull(park.getTickets().find("TG02-1"));
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(error(run("GROUPTICKET CREATE;TG01;G01;2026-10-15")));
        assertTrue(error(run("TICKET CREATE;TG01-1;u1@correo.com;2026-10-15")));
    }

    @Test
    void lockedGroupAndMembersUntilTicketIsHistorical() {
        groupOf(2);
        createUser(3);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SYSTEM DATE;2026-10-15");

        assertTrue(error(run("GROUP ADD;G01;u3@correo.com")));
        assertTrue(error(run("GROUP REMOVE;G01;u1@correo.com")));
        assertTrue(error(run("GROUP DELETE;G01")));
        assertTrue(error(run("USER DELETE;u1@correo.com;Clave1!")));

        run("SYSTEM DATE;2026-10-16");
        assertTrue(ok(run("GROUP ADD;G01;u3@correo.com")));
        assertTrue(ok(run("GROUP REMOVE;G01;u1@correo.com")));
        assertTrue(ok(run("USER DELETE;u2@correo.com;Clave1!")));
        assertTrue(ok(run("GROUP DELETE;G01")));
        assertNotNull(groupTicket("TG01"));
    }

    @Test
    void constituentCannotBeDeletedAlone() {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(error(run("TICKET DELETE;TG01-1")));
        assertTrue(error(run("GROUPTICKET DELETE;TG01-1")));
        assertEquals(2, groupTicket("TG01").getTickets().size());
    }

    @Test
    void deletesFutureGroupTicketWithAllConstituents() {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(ok(run("GROUPTICKET DELETE;TG01")));
        assertNull(park.getTickets().find("TG01"));
        assertNull(park.getTickets().find("TG01-1"));
        assertNull(park.getTickets().find("TG01-2"));
        assertTrue(ok(run("GROUP DELETE;G01")));
    }

    @Test
    void currentGroupTicketIsNotDeleted() {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SYSTEM DATE;2026-10-15");
        assertTrue(error(run("GROUPTICKET DELETE;TG01")));
        assertTrue(error(run("GROUPTICKET DELETE;NOPE")));
        assertEquals(2, groupTicket("TG01").getTickets().size());
    }

    @Test
    void restartRestoresGroupTicketsAndLocks() throws IOException {
        groupOf(3);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        restart();

        GroupTicket tg = groupTicket("TG01");
        assertEquals(new BigDecimal("108.00"), tg.getPrice());
        assertSame(park.getGroups().find("G01"), tg.getGroup());
        assertSame(park.getUsers().findByEmail("u2@correo.com"), tg.getTickets().get(1).getUser());
        assertTrue(error(run("GROUP DELETE;G01")));
    }

    @Test
    void historicalGroupTicketSurvivesGroupDeletion() throws IOException {
        groupOf(2);
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SYSTEM DATE;2026-10-16");
        run("GROUP DELETE;G01");
        restart();

        GroupTicket tg = groupTicket("TG01");
        assertEquals("G01", tg.getGroup().getId());
        assertEquals("Grupo", tg.getGroup().getName());
        assertNull(park.getGroups().find("G01"));
    }

    @Test
    void discountRates() {
        assertEquals(0, BigDecimal.ZERO.compareTo(TicketManager.discount(2)));
        assertEquals(0, new BigDecimal("0.10").compareTo(TicketManager.discount(3)));
        assertEquals(0, new BigDecimal("0.10").compareTo(TicketManager.discount(5)));
        assertEquals(0, new BigDecimal("0.20").compareTo(TicketManager.discount(6)));
        assertEquals(0, new BigDecimal("0.20").compareTo(TicketManager.discount(10)));
        assertEquals(0, new BigDecimal("0.50").compareTo(TicketManager.discount(11)));
    }
}
