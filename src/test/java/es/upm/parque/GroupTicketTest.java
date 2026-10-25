package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class GroupTicketTest extends ParkTestSupport {

    @BeforeEach
    void date() {
        ok("SYSTEM DATE;2026-10-01");
    }

    private GroupTicket groupTicket(String id) {
        return assertInstanceOf(GroupTicket.class, controller.getTicketManager().find(id));
    }

    private void groupOf(String groupId, String... emails) {
        ok("GROUP CREATE;" + groupId + ";Grupo " + groupId);
        for (String email : emails) {
            ok("GROUP ADD;" + groupId + ";" + email);
        }
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    public void appliesDiscountBrackets(int members, String expected) {
        groupOf("G01", createUsers(members));
        assertEquals("OK GROUP TICKET CREATED -> " + expected, run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals(new BigDecimal(expected), groupTicket("TG01").getPrice());
    }

    @Test
    public void generatesOneTicketPerMemberAtCurrentPrice() {
        createAna();
        createPedro();
        groupOf("G01", ANA, PEDRO);
        ok("SYSTEM PRICE;50.00");
        assertEquals("OK GROUP TICKET CREATED -> 100.00", run("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        GroupTicket tg01 = groupTicket("TG01");
        assertSame(controller.getGroupManager().find("G01"), tg01.getGroup());
        assertEquals(List.of(user(ANA), user(PEDRO)), tg01.getUsers());
        for (IndividualTicket generated : tg01.getTickets()) {
            assertEquals("50.00", generated.getPrice().toPlainString());
            assertEquals(tg01.getDate(), generated.getDate());
            assertSame(generated, controller.getTicketManager().find(generated.getId()));
        }
    }

    @Test
    public void rejectsInvalidPurchases() {
        createAna();
        groupOf("G01", ANA);
        groupOf("G02");
        fails("GROUPTICKET CREATE;TG01;G99;2026-10-15");
        fails("GROUPTICKET CREATE;TG01;G02;2026-10-15");
        fails("GROUPTICKET CREATE;TG01;G01;2026-09-30");
        ok("TICKET CREATE;TG01-1;" + ANA + ";2026-10-15");
        fails("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("GROUPTICKET CREATE;TG02;G01;2026-10-15");
        fails("GROUPTICKET CREATE;TG02;G01;2026-10-16");
        fails("TICKET CREATE;TG02;" + ANA + ";2026-10-16");
    }

    @Test
    public void lockedGroupCannotChangeUntilTicketIsPast() {
        createAna();
        createPedro();
        String[] others = createUsers(1);
        groupOf("G01", ANA, PEDRO);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        fails("GROUP ADD;G01;" + others[0]);
        fails("GROUP REMOVE;G01;" + ANA);
        fails("GROUP DELETE;G01");
        ok("SYSTEM DATE;2026-10-15");
        fails("GROUP ADD;G01;" + others[0]);
        ok("SYSTEM DATE;2026-10-16");
        ok("GROUP ADD;G01;" + others[0]);
        assertEquals(2, groupTicket("TG01").getTickets().size(), "composition is fixed at purchase");
        ok("GROUP REMOVE;G01;" + ANA);
        ok("GROUP DELETE;G01");
    }

    @Test
    public void generatedTicketsBlockMemberDeletion() {
        createAna();
        groupOf("G01", ANA);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        fails("USER DELETE;" + ANA + ";Clave1!");
        ok("SYSTEM DATE;2026-10-16");
        ok("USER DELETE;" + ANA + ";Clave1!");
    }

    @Test
    public void deletesFutureGroupTicketWithItsTickets() {
        createAna();
        createPedro();
        groupOf("G01", ANA, PEDRO);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        fails("TICKET DELETE;TG01-1");
        fails("TICKET DELETE;TG01");
        assertEquals("OK GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG01"));
        assertNull(controller.getTicketManager().find("TG01"));
        assertNull(controller.getTicketManager().find("TG01-1"));
        assertNull(controller.getTicketManager().find("TG01-2"));
        ok("GROUP ADD;G01;" + createUsers(1)[0]);
        ok("USER DELETE;" + ANA + ";Clave1!");
    }

    @Test
    public void groupTicketForTodayCannotBeDeleted() {
        createAna();
        groupOf("G01", ANA);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        fails("GROUPTICKET DELETE;T001");
        ok("SYSTEM DATE;2026-10-15");
        fails("GROUPTICKET DELETE;TG01");
        assertNotNull(controller.getTicketManager().find("TG01-1"));
    }

    @Test
    public void groupTicketsSurviveRestart() {
        createAna();
        createPedro();
        groupOf("G01", ANA, PEDRO);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        restart();
        ok("SYSTEM DATE;2026-10-01");
        GroupTicket tg01 = groupTicket("TG01");
        assertSame(controller.getGroupManager().find("G01"), tg01.getGroup());
        assertEquals("80.00", tg01.getPrice().toPlainString());
        assertEquals(List.of(user(ANA), user(PEDRO)), tg01.getUsers());
        assertSame(controller.getTicketManager().find("TG01-1"), tg01.getTickets().get(0));
        fails("GROUP DELETE;G01");
        fails("TICKET DELETE;TG01-2");
    }

    @Test
    public void historyOfDeletedGroupIsRestored() {
        createAna();
        groupOf("G01", ANA);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SYSTEM DATE;2026-10-16");
        ok("GROUP DELETE;G01");
        restart();
        assertEquals("G01", groupTicket("TG01").getGroup().getId());
        assertNull(controller.getGroupManager().find("G01"));
    }
}
