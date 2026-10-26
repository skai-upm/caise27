package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
        "11,50,220.00", "12,50,240.00"})
    void discountBracketsAndBoundaries(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountFor(members));
        Group group = new Group("G01", "Grupo");
        List<IndividualTicket> parts = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            User u = new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
            group.addMember(u);
            parts.add(new IndividualTicket("TG01-" + (i + 1), LocalDate.of(2026, 10, 15), u, new BigDecimal("40.00")));
        }
        GroupTicket ticket = GroupTicket.purchase("TG01", LocalDate.of(2026, 10, 15), group, parts);
        assertEquals(new BigDecimal(total), ticket.getPrice());
        assertEquals(members, ticket.getUsers().size());
        assertEquals(members, ticket.getTickets().size());
    }
}
