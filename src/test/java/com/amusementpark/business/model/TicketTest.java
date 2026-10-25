package com.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    public static User user(int n) {
        return new User("U" + n, "S", "dni" + n, "u" + n + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    @Test
    public void dateHelpers() {
        Ticket ticket = new IndividualTicket("T001", DAY, FORTY, user(1));
        assertTrue(ticket.isActiveOn(DAY));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
        assertTrue(ticket.isFutureOn(DAY.minusDays(1)));
        assertFalse(ticket.isFutureOn(DAY));
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    public void groupPriceByBracket(int members, String expected) {
        assertEquals(new BigDecimal(expected), GroupTicket.totalPrice(FORTY, members));
    }

    @Test
    public void purchaseGeneratesOneTicketPerMember() {
        Group group = new Group("G01", "Familia");
        group.add(user(1));
        group.add(user(2));
        group.add(user(3));
        GroupTicket groupTicket = GroupTicket.purchase("TG01", DAY, FORTY, group);
        assertEquals(3, groupTicket.getTickets().size());
        assertEquals("TG01-1", groupTicket.getTickets().get(0).getId());
        assertEquals(new BigDecimal("108.00"), groupTicket.getPrice());
        assertSame(group, groupTicket.getGroup());
        assertEquals(group.getMembers(), groupTicket.getUsers());

        group.add(user(4));
        assertEquals(3, groupTicket.getUsers().size());
    }

    @Test
    public void individualTicketUsersIsItsUser() {
        User user = user(1);
        assertEquals(List.of(user), new IndividualTicket("T1", DAY, FORTY, user).getUsers());
    }
}
