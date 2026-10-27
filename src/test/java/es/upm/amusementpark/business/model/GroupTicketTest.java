package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static final BigDecimal BASE = new BigDecimal("40.00");
    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @Test
    void discountBrackets() {
        assertEquals(new BigDecimal("40.00"), GroupTicket.discountedPrice(BASE, 2));
        assertEquals(new BigDecimal("36.00"), GroupTicket.discountedPrice(BASE, 3));
        assertEquals(new BigDecimal("36.00"), GroupTicket.discountedPrice(BASE, 5));
        assertEquals(new BigDecimal("32.00"), GroupTicket.discountedPrice(BASE, 6));
        assertEquals(new BigDecimal("32.00"), GroupTicket.discountedPrice(BASE, 10));
        assertEquals(new BigDecimal("20.00"), GroupTicket.discountedPrice(BASE, 11));
    }

    @Test
    void totalIsSumOfComposedTickets() {
        Group group = new Group("G02", "Seis");
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            User user = new User("U" + i, "S", "0000000" + i + "X", "u" + i + "@correo.com", "Clave1!", "6",
                    "1234567890123456");
            composition.add(new IndividualTicket("TG02:" + user.getDni(), DAY, user,
                    GroupTicket.discountedPrice(BASE, 6)));
        }
        GroupTicket ticket = new GroupTicket("TG02", DAY, group, composition);
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertSame(group, ticket.getGroup());
        assertTrue(composition.get(0).isPartOfGroupTicket());
        assertSame(ticket, composition.get(0).getGroupTicket());
        assertThrows(UnsupportedOperationException.class, () -> ticket.getComposition().clear());
        ticket.clearGroup();
        assertEquals(null, ticket.getGroup());
    }
}
