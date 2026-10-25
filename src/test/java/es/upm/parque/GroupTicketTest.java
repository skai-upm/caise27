package es.upm.parque;

import static es.upm.parque.ParkTestSupport.dni;
import static es.upm.parque.ParkTestSupport.email;
import static es.upm.parque.ParkTestSupport.error;
import static es.upm.parque.ParkTestSupport.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

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
        ParkController park = ParkTestSupport.park(dir);
        ParkTestSupport.group(park, "G01", members);
        assertEquals("OK GROUP TICKET CREATED -> " + total, park.createGroupTicket("TG01", "G01", "2026-10-15"));
        GroupTicket ticket = (GroupTicket) park.getTicketManager().find("TG01");
        assertEquals(members, ticket.getTickets().size());
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }

    @Test
    void generatesOneTicketPerMemberReferencingTheUser() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        Group group = ParkTestSupport.group(park, "G01", 3);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        GroupTicket ticket = (GroupTicket) park.getTicketManager().find("TG01");
        assertSame(group, ticket.getGroup());
        for (int i = 0; i < 3; i++) {
            IndividualTicket internal = ticket.getTickets().get(i);
            assertSame(group.getMembers().get(i), internal.getUser());
            assertEquals("TG01-" + dni(i + 1), internal.getId());
            assertEquals(new BigDecimal("40.00"), internal.getPrice());
        }
        assertInstanceOf(IndividualTicket.class, park.getTicketManager().find("TG01-" + dni(1)));
    }

    @Test
    void keepsCompositionOfPurchaseTime() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ParkTestSupport.group(park, "G01", 3);
        ParkTestSupport.user(park, 4);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(park.setSystemDate("2026-10-16"));
        ok(park.addToGroup("G01", email(4)));
        assertEquals(3, ((GroupTicket) park.getTicketManager().find("TG01")).getTickets().size());
    }

    @Test
    void rejectsInvalidPurchases() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ok(park.createGroup("G00", "Vacio"));
        error(park.createGroupTicket("TG00", "G00", "2026-10-15"));
        error(park.createGroupTicket("TG00", "G99", "2026-10-15"));
        ParkTestSupport.group(park, "G01", 2);
        error(park.createGroupTicket("TG01", "G01", "2026-09-30"));
        ok(park.createTicket("T001", email(1), "2026-10-15"));
        error(park.createGroupTicket("T001", "G01", "2026-10-15"));
        ok(park.createTicket("TG02-" + dni(1), email(1), "2026-10-15"));
        error(park.createGroupTicket("TG02", "G01", "2026-10-15"));
        assertEquals(0, park.getTicketManager().getGroupTickets().size());
    }

    @Test
    void deletesFutureGroupTicketWithItsInternalTickets() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ParkTestSupport.group(park, "G01", 2);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(park.createGroupTicket("TG02", "G01", "2026-10-15"));
        error(park.deleteTicket("TG01-" + dni(1)));
        ok(park.deleteGroupTicket("TG01"));
        assertNull(park.getTicketManager().find("TG01"));
        assertNull(park.getTicketManager().find("TG01-" + dni(1)));
        ok(park.setSystemDate("2026-10-15"));
        error(park.deleteGroupTicket("TG02"));
        error(park.deleteGroupTicket("T404"));
    }

    @Test
    void internalTicketsProtectTheirUsers() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ParkTestSupport.group(park, "G01", 2);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        error(park.deleteUser(email(1), "Clave1!"));
        ok(park.setSystemDate("2026-10-16"));
        ok(park.deleteUser(email(1), "Clave1!"));
        GroupTicket ticket = (GroupTicket) park.getTicketManager().find("TG01");
        assertEquals(1, ticket.getTickets().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ParkTestSupport.group(park, "G01", 3);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        ParkController restarted = ParkTestSupport.restart(dir);
        GroupTicket ticket = (GroupTicket) restarted.getTicketManager().find("TG01");
        assertNotNull(ticket);
        assertSame(restarted.getGroupManager().find("G01"), ticket.getGroup());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertEquals(3, ticket.getTickets().size());
        assertSame(restarted.getUserManager().findByEmail(email(2)), ticket.getTickets().get(1).getUser());
        error(restarted.addToGroup("G01", email(1)));
    }
}
