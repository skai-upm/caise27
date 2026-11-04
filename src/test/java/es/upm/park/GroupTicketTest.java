package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    static final BigDecimal PRICE = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
        "11,50,220.00", "20,50,400.00"})
    void discountBrackets(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountPercent(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalFor(PRICE, members));
    }

    @Test
    void otherPriceAndRounding() {
        assertEquals(new BigDecimal("162.00"), GroupTicket.totalFor(new BigDecimal("60.00"), 3));
        assertEquals(new BigDecimal("30.02"), GroupTicket.totalFor(new BigDecimal("11.12"), 3));
    }

    @Test
    void purchaseComposesOneTicketPerMember() {
        Group g = new Group("G01", "Familia");
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "1", "1234567890123456");
        g.add(ana);
        g.add(pedro);
        GroupTicket t = GroupTicket.purchase("TG01", g, LocalDate.of(2026, 10, 15), PRICE);
        assertEquals(2, t.getConstituents().size());
        assertSame(ana, t.getConstituents().get(0).getOwner());
        assertEquals("TG01-2", t.getConstituents().get(1).getId());
        assertEquals(PRICE, t.getConstituents().get(1).getPrice());
        assertEquals(new BigDecimal("80.00"), t.getPrice());
        assertTrue(t.belongsTo(g));
        Ticket common = t;
        assertEquals(LocalDate.of(2026, 10, 15), common.getDate());
        g.add(new User("Luis", "M", "11111111H", "luis@correo.com", "Clave1!", "1", "1234567890123456"));
        assertEquals(2, t.getConstituents().size(), "composition fixed at purchase");
    }
}
