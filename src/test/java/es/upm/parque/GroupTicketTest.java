package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
            "11,50,220.00"})
    void discountBrackets(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountFor(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalPrice(new BigDecimal("40.00"), members));
    }
}
