package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    private static GroupTicket groupTicketOf(int members) {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            User user = new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
            tickets.add(new IndividualTicket("TG01-" + (i + 1), DATE, user, new BigDecimal("40.00")));
        }
        return new GroupTicket("TG01", DATE, new Group("G01", "Grupo"), tickets);
    }

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00", "11,50,220.00"})
    void discountBrackets(int members, int discount, String total) {
        GroupTicket ticket = groupTicketOf(members);
        assertEquals(discount, ticket.getDiscountPercent());
        assertEquals(new BigDecimal(total), ticket.getPrice());
    }

    @Test
    void compositionIsReadOnly() {
        GroupTicket ticket = groupTicketOf(2);
        assertEquals(2, ticket.getTickets().size());
        assertThrows(UnsupportedOperationException.class, () -> ticket.getTickets().clear());
    }
}
