package es.upm.etsisi.amusementpark.business.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.upm.etsisi.amusementpark.business.user.User;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static User user(int n) {
        return new User("U" + n, "S", String.format("%08d", n) + "X", "u" + n + "@correo.com", "Clave1!", "6", "1");
    }

    @Test
    void individualTicketCostsBasePrice() {
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), FORTY, 0);
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(List.of(t.getUser()), t.getUsers());
    }

    @Test
    void activityAndValidity() {
        Ticket t = new IndividualTicket("T001", DAY, user(1), FORTY, 0);
        assertTrue(t.isActiveOn(DAY));
        assertTrue(t.isActiveOn(DAY.minusDays(1)));
        assertFalse(t.isActiveOn(DAY.plusDays(1)));
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.plusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "40,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "6,192.00", "11,220.00"})
    void groupTotalIsSumOfDiscountedTickets(int members, String total) {
        int discount = GroupTicket.discountFor(members);
        List<IndividualTicket> parts = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            User u = user(i);
            parts.add(new IndividualTicket(GroupTicket.compositionIdentifier("TG", u), DAY, u, FORTY, discount));
        }
        GroupTicket g = new GroupTicket("TG", DAY, null, parts);
        assertEquals(new BigDecimal(total), g.getPrice());
        assertEquals(members, g.getUsers().size());
    }

    @Test
    void compositionIsFixed() {
        List<IndividualTicket> parts = new ArrayList<>(List.of(new IndividualTicket("TG:1", DAY, user(1), FORTY, 0)));
        GroupTicket g = new GroupTicket("TG", DAY, null, parts);
        parts.clear();
        assertEquals(1, g.getComposition().size());
    }
}
