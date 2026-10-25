package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.upm.parque.Fixtures;

class GroupTicketTest {

    private static final BigDecimal FORTY = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "30,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    void totalAppliesDiscountToSumOfTickets(int members, String total) {
        GroupTicket ticket = new GroupTicket("TG01", LocalDate.of(2026, 10, 15), new Group("G01", "Grupo"),
                Fixtures.users(members), Collections.nCopies(members, FORTY), GroupTicket.discountFor(members));
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getTickets().size());
    }

    @Test
    void composesOneIndividualTicketPerMember() {
        Group group = new Group("G01", "Familia");
        List<User> members = Fixtures.users(2);
        GroupTicket ticket = new GroupTicket("TG01", LocalDate.of(2026, 10, 15), group, members,
                List.of(FORTY, new BigDecimal("60.00")), 0);
        assertSame(group, ticket.getGroup());
        assertEquals(0, ticket.getDiscountPercent());
        assertEquals("TG01-1", ticket.getTickets().get(0).getId());
        assertEquals("TG01-2", ticket.getTickets().get(1).getId());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getTickets().get(1).getDate());
        assertSame(members.get(1), ticket.getTickets().get(1).getHolder());
        assertEquals(members, ticket.getVisitors());
        assertEquals(new BigDecimal("100.00"), ticket.getPrice());
    }
}
