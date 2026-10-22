package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final String[] DNIS = {"12345678Z", "87654321X", "11111111H", "00000000T", "22222222J",
        "33333333P", "44444444A", "55555555K", "66666666Q", "77777777B", "88888888Y", "99999999R"};

    private static Group groupOf(int members) {
        Group group = new Group("G", "Grupo");
        for (int i = 0; i < members; i++) {
            group.add(new User("U" + i, "S", DNIS[i], "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456"));
        }
        return group;
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "40,50"})
    void discountBrackets(int members, int percent) {
        assertEquals(percent, GroupDiscount.forMembers(members).getPercent());
    }

    @ParameterizedTest
    @CsvSource({"2,40.00,80.00", "2,45.50,91.00", "3,40.00,108.00", "6,40.00,192.00", "11,40.00,220.00"})
    void totalIsSumOfDiscountedComponents(int members, String base, String total) {
        GroupTicket ticket = GroupTicket.purchase("TG", DAY, groupOf(members), new BigDecimal(base));
        assertEquals(members, ticket.getComposition().size());
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }

    @Test
    void sixMembersPay32Each() {
        GroupTicket ticket = GroupTicket.purchase("TG02", DAY, groupOf(6), new BigDecimal("40.00"));
        for (IndividualTicket member : ticket.getComposition()) {
            assertEquals(new BigDecimal("32.00"), member.getPrice());
            assertEquals(20, member.getDiscountPercent());
        }
        assertEquals(20, ticket.getDiscountPercent());
    }

    @Test
    void componentsUseDerivedIdsAndReferenceTheirUsers() {
        Group group = groupOf(2);
        GroupTicket ticket = GroupTicket.purchase("TG01", DAY, group, new BigDecimal("40.00"));
        assertEquals("TG01:12345678Z", ticket.getComposition().get(0).getId());
        assertEquals("TG01:87654321X", ticket.getComposition().get(1).getId());
        assertSame(group.getMembers().get(0), ticket.getComposition().get(0).getUser());
        assertEquals(group.getMembers(), ticket.getHolders());
        assertEquals(DAY, ticket.getComposition().get(1).getDate());
        assertSame(group, ticket.getGroup());
    }

    @Test
    void compositionIsFixedAtPurchase() {
        Group group = groupOf(3);
        GroupTicket ticket = GroupTicket.purchase("TG01", DAY, group, new BigDecimal("40.00"));
        User removed = group.getMembers().get(0);
        group.remove(removed);
        assertEquals(3, ticket.getComposition().size());
        assertEquals(removed, ticket.getHolders().get(0));
        List<IndividualTicket> composition = ticket.getComposition();
        assertThrows(UnsupportedOperationException.class, composition::clear);
    }

    @Test
    void groupReferenceCanBeDetached() {
        GroupTicket ticket = GroupTicket.purchase("TG01", DAY, groupOf(1), new BigDecimal("40.00"));
        ticket.detachGroup();
        assertNull(ticket.getGroup());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
    }
}
