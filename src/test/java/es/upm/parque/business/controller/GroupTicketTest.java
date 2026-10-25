package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.dni;
import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class GroupTicketTest {

    @TempDir
    Path dir;

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "20,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void purchaseAppliesDiscountAndAnswersTotal(int members, String total) throws IOException {
        TestContext ctx = TestContext.start(dir);
        ctx.group("G01", members);
        assertEquals("OK GROUP TICKET CREATED -> " + total, ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        GroupTicket ticket = (GroupTicket) ctx.tickets().find("TG01");
        assertEquals(members, ticket.getTickets().size());
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }

    @Test
    void generatesOneTicketPerMemberReferencingTheUser() throws IOException {
        TestContext ctx = TestContext.start(dir);
        Group group = ctx.group("G01", 3);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        GroupTicket ticket = (GroupTicket) ctx.tickets().find("TG01");
        assertSame(group, ticket.getGroup());
        for (int i = 0; i < 3; i++) {
            IndividualTicket internal = ticket.getTickets().get(i);
            assertSame(group.getMembers().get(i), internal.getUser());
            assertEquals("TG01-" + dni(i + 1), internal.getId());
            assertEquals(new BigDecimal("40.00"), internal.getPrice());
        }
        assertInstanceOf(IndividualTicket.class, ctx.tickets().find("TG01-" + dni(1)));
    }

    @Test
    void keepsCompositionOfPurchaseTime() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ctx.group("G01", 3);
        ctx.user(4);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(ctx.park().setSystemDate("2026-10-16"));
        ok(ctx.groups().addToGroup("G01", email(4)));
        assertEquals(3, ((GroupTicket) ctx.tickets().find("TG01")).getTickets().size());
    }

    @Test
    void rejectsInvalidPurchases() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ok(ctx.groups().createGroup("G00", "Vacio"));
        error(ctx.tickets().createGroupTicket("TG00", "G00", "2026-10-15"));
        error(ctx.tickets().createGroupTicket("TG00", "G99", "2026-10-15"));
        ctx.group("G01", 2);
        error(ctx.tickets().createGroupTicket("TG01", "G01", "2026-09-30"));
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        error(ctx.tickets().createGroupTicket("T001", "G01", "2026-10-15"));
        ok(ctx.tickets().createTicket("TG02-" + dni(1), email(1), "2026-10-15"));
        error(ctx.tickets().createGroupTicket("TG02", "G01", "2026-10-15"));
        assertEquals(0, ctx.tickets().getGroupTickets().size());
    }

    @Test
    void deletesFutureGroupTicketWithItsInternalTickets() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ctx.group("G01", 2);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(ctx.tickets().createGroupTicket("TG02", "G01", "2026-10-15"));
        error(ctx.tickets().deleteTicket("TG01-" + dni(1)));
        ok(ctx.tickets().deleteGroupTicket("TG01"));
        assertNull(ctx.tickets().find("TG01"));
        assertNull(ctx.tickets().find("TG01-" + dni(1)));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.tickets().deleteGroupTicket("TG02"));
        error(ctx.tickets().deleteGroupTicket("T404"));
    }

    @Test
    void internalTicketsProtectTheirUsers() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ctx.group("G01", 2);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        error(ctx.users().deleteUser(email(1), "Clave1!"));
        ok(ctx.park().setSystemDate("2026-10-16"));
        ok(ctx.users().deleteUser(email(1), "Clave1!"));
        GroupTicket ticket = (GroupTicket) ctx.tickets().find("TG01");
        assertEquals(1, ticket.getTickets().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ctx.group("G01", 3);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        TestContext restarted = ctx.restart();
        GroupTicket ticket = (GroupTicket) restarted.tickets().find("TG01");
        assertNotNull(ticket);
        assertSame(restarted.groups().find("G01"), ticket.getGroup());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertEquals(3, ticket.getTickets().size());
        assertSame(restarted.users().findByEmail(email(2)), ticket.getTickets().get(1).getUser());
        error(restarted.groups().addToGroup("G01", email(1)));
    }
}
