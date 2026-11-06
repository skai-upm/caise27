package es.upm.park.business.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal BASE = new BigDecimal("40.00");

    private static Map<String, String> members(int count) {
        Map<String, String> members = new LinkedHashMap<>();
        for (int i = 1; i <= count; i++) {
            members.put("u" + i + "@correo.com", "DNI" + i);
        }
        return members;
    }

    private static BigDecimal unitPrice(int members) {
        return GroupTicket.purchase("TG", new Group("G", "g"), DAY, BASE, members(members))
                .getComposition().get(0).getPrice();
    }

    @Test
    void discountBrackets() {
        assertEquals(new BigDecimal("40.00"), unitPrice(2));
        assertEquals(new BigDecimal("36.00"), unitPrice(3));
        assertEquals(new BigDecimal("36.00"), unitPrice(5));
        assertEquals(new BigDecimal("32.00"), unitPrice(6));
        assertEquals(new BigDecimal("32.00"), unitPrice(10));
        assertEquals(new BigDecimal("20.00"), unitPrice(11));
    }

    @Test
    void totalIsSumOfComposedTickets() {
        GroupTicket ticket = GroupTicket.purchase("TG02", new Group("G02", "g"), DAY, BASE, members(6));
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertEquals(6, ticket.getComposition().size());
        assertEquals("TG02:DNI1", ticket.getComposition().get(0).getId());
        assertEquals("u1@correo.com", ticket.getComposition().get(0).getOwnerEmail());
        assertTrue(ticket.getComposition().stream().allMatch(IndividualTicket::isPartOfGroupTicket));
        assertTrue(ticket.getComposition().stream().allMatch(t -> t.getDate().equals(DAY)));
        assertEquals(6, ticket.admittedUsers().size());
        assertEquals("u1@correo.com", ticket.admittedUsers().get(0));
    }
}
