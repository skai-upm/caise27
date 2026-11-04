package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final BigDecimal P40 = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00",
        "10,20,320.00", "11,50,220.00", "20,50,400.00"})
    void discountBracketsAndBoundaries(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountPercent(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalPrice(P40, members));
    }

    @Test
    void purchaseCreatesOneTicketPerCurrentMember() {
        Group g = new Group("G01", "Familia Garcia");
        User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        g.addMember(ana);
        g.addMember(pedro);
        LocalDate date = LocalDate.of(2026, 10, 15);
        GroupTicket t = GroupTicket.purchase("TG01", date, g, P40);

        assertEquals(2, t.getTickets().size());
        assertSame(ana, t.getTickets().get(0).getUser());
        assertEquals("TG01-12345678Z", t.getTickets().get(0).getId());
        assertEquals(date, t.getTickets().get(1).getDate());
        assertEquals(P40, t.getTickets().get(1).getPrice());
        assertEquals(new BigDecimal("80.00"), t.getPrice());
        assertSame(g, t.getGroup());

        g.addMember(new User("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "6", "1111222233334444"));
        assertEquals(2, t.getTickets().size(), "composition frozen at purchase");
        assertThrows(UnsupportedOperationException.class, () -> t.getTickets().clear());
    }
}
