package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static List<User> users(int n) {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String dni = String.format("%08d", i) + "X";
            list.add(new User("U" + i, "S", dni, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456"));
        }
        return list;
    }

    private static GroupTicket ticket(int members) {
        Group g = new Group("G01", "F");
        List<User> us = users(members);
        us.forEach(g::addMember);
        return new GroupTicket("TG01", DAY, FORTY, g, us);
    }

    @Test
    void discountBrackets() {
        assertEquals(0, BigDecimal.ZERO.compareTo(GroupDiscount.forMembers(2)));
        assertEquals(new BigDecimal("0.10"), GroupDiscount.forMembers(3));
        assertEquals(new BigDecimal("0.10"), GroupDiscount.forMembers(5));
        assertEquals(new BigDecimal("0.20"), GroupDiscount.forMembers(6));
        assertEquals(new BigDecimal("0.20"), GroupDiscount.forMembers(10));
        assertEquals(new BigDecimal("0.50"), GroupDiscount.forMembers(11));
    }

    @Test
    void constituentPricesPerBracket() {
        assertEquals(new BigDecimal("40.00"), ticket(2).getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("36.00"), ticket(3).getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("36.00"), ticket(5).getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), ticket(10).getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("20.00"), ticket(11).getComposition().get(0).getPrice());
    }

    @Test
    void sixMembersAtFortyCost192() {
        GroupTicket t = ticket(6);
        assertEquals(6, t.getComposition().size());
        assertEquals(new BigDecimal("32.00"), t.getComposition().get(5).getPrice());
        assertEquals(new BigDecimal("192.00"), t.getPrice());
    }

    @Test
    void compositionIdentifiersAndReferences() {
        GroupTicket t = ticket(2);
        assertEquals(List.of("TG01", "TG01:00000000X", "TG01:00000001X"), t.ids());
        IndividualTicket c = t.findConstituent("TG01:00000001X").orElseThrow();
        assertEquals("00000001X", c.getUser().getDni());
        assertEquals(DAY, c.getDate());
        assertTrue(t.involves(c.getUser()));
        assertFalse(t.findConstituent("TG01:nope").isPresent());
        assertEquals("G01", t.getGroup().getId());
        Group g = t.getGroup();
        g.addMember(new User("N", "S", "99999999R", "n@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(2, t.getComposition().size());
        t.detachGroup();
        assertNull(t.getGroup());
        assertEquals("G01", t.getGroupId());
        assertSame(c, t.getComposition().get(1));
    }
}
