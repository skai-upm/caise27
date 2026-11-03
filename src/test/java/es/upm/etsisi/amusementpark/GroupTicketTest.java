package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    static final BigDecimal FORTY = new BigDecimal("40.00");
    static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    static Group groupOf(int members) {
        Group g = new Group("G01", "Familia");
        for (int i = 0; i < members; i++) {
            g.addMember(GroupTest.user(String.format("%08dX", i), "u" + i + "@correo.com"));
        }
        return g;
    }

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
        "11,50,220.00", "20,50,400.00"})
    void discountBracketsAndBoundaries(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountFor(members));
        GroupTicket t = new GroupTicket("TG01", groupOf(members), DAY, FORTY);
        assertEquals(total, t.getPrice().toPlainString());
        assertEquals(members, t.getTickets().size());
    }

    @Test
    void oneConstituentTicketPerMemberReferringToTheUser() {
        Group g = groupOf(2);
        GroupTicket t = new GroupTicket("TG01", g, DAY, FORTY);
        assertTrue(t.belongsTo(g));
        assertSame(g, t.getGroup().orElseThrow());
        for (int i = 0; i < 2; i++) {
            Ticket c = t.getTickets().get(i);
            assertSame(g.getMembers().get(i), c.getUser().orElseThrow());
            assertEquals("TG01-" + (i + 1), c.getId());
            assertEquals(DAY, c.getDate());
            assertEquals(FORTY, c.getPrice());
        }
    }

    @Test
    void compositionIsFrozenAtPurchase() {
        Group g = groupOf(2);
        GroupTicket t = new GroupTicket("TG01", g, DAY, FORTY);
        g.addMember(GroupTest.user("99999999R", "nuevo@correo.com"));
        g.removeMember(g.getMembers().get(0));
        assertEquals(2, t.getTickets().size());
        assertEquals("80.00", t.getPrice().toPlainString());
    }

    @Test
    void commonAbstraction() {
        List<ParkTicket> all = List.of(new GroupTicket("TG01", groupOf(3), DAY, FORTY),
                new Ticket("T001", GroupTest.user("12345678Z", "a@correo.com"), DAY, FORTY));
        assertEquals(new BigDecimal("148.00"), all.stream().map(ParkTicket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertTrue(all.stream().allMatch(t -> t.isValidOn(DAY)));
        assertTrue(all.stream().noneMatch(t -> t.isValidOn(DAY.plusDays(1))));
    }
}
