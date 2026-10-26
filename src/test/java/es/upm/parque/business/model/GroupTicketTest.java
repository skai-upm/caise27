package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    private static Group groupOf(int members) {
        Group group = new Group("G01", "Grupo");
        for (int i = 0; i < members; i++) {
            group.addMember(new User("U" + i, "S", "0000000" + i + "X", "u" + i + "@correo.com", "Clave1!", "600",
                "1234567890123456"));
        }
        return group;
    }

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
        "11,50,220.00", "20,50,400.00"})
    void discountBrackets(int members, int discount, String total) {
        Group group = groupOf(members);
        GroupTicket ticket = new GroupTicket("TG01", DATE, group, new BigDecimal("40.00"));

        assertEquals(discount, GroupTicket.discountFor(members));
        assertEquals(discount, ticket.getDiscountPercent());
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getComponents().size());
        for (int i = 0; i < members; i++) {
            IndividualTicket component = ticket.getComponents().get(i);
            assertEquals("TG01-" + (i + 1), component.getId());
            assertSame(group.getMembers().get(i), component.getOwner());
            assertEquals(DATE, component.getDate());
            assertEquals(new BigDecimal("40.00"), component.getPrice());
            assertEquals("TG01", component.getGroupTicketId());
        }
        assertSame(group, ticket.getGroup());
    }

    @ParameterizedTest
    @CsvSource({"3,33.33,89.99", "6,10.01,48.05"})
    void totalIsRoundedToTwoDecimals(int members, String price, String total) {
        GroupTicket ticket = new GroupTicket("TG01", DATE, groupOf(members), new BigDecimal(price));
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }
}
