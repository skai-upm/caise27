package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class GroupTicketTest {

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
        "11,50,220.00", "20,50,400.00"})
    void discountBrackets(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountFor(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalFor(new BigDecimal("40.00"), members));
    }

    @Test
    void totalIsRoundedToCents() {
        assertEquals(new BigDecimal("30.00"), GroupTicket.totalFor(new BigDecimal("11.11"), 3));
    }

    @Test
    void usersComeFromInternalTickets() {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        LocalDate day = LocalDate.of(2026, 10, 15);
        IndividualTicket a = new IndividualTicket("TG01-1", day, ana, new BigDecimal("40.00"));
        IndividualTicket p = new IndividualTicket("TG01-2", day, pedro, new BigDecimal("40.00"));
        Group group = new Group("G01", "F");
        GroupTicket ticket = new GroupTicket("TG01", day, group, new BigDecimal("80.00"), List.of(a, p));

        assertEquals(List.of(ana), a.getUsers());
        assertEquals(List.of(ana, pedro), ticket.getUsers());
        assertEquals(group, ticket.getGroup());
        assertTrue(ticket.contains(a));
        assertTrue(ticket.removeTicket(a));
        assertFalse(ticket.contains(a));
        assertEquals(List.of(pedro), ticket.getUsers());
    }
}
