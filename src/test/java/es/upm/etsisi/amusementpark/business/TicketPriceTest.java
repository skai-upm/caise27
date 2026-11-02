package es.upm.etsisi.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class TicketPriceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @ParameterizedTest
    @CsvSource({"0,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @Test
    void passPricesUseStoredTicketPrice() {
        User u = new User("A", "B", "12345678Z", "a@b.com", "h", "6", "1");
        IndividualTicket discounted = new IndividualTicket("TG:1", DAY, u, new BigDecimal("40.00"), 10);
        assertEquals(new BigDecimal("36.00"), discounted.getPrice());
        assertEquals(new BigDecimal("7.20"), new SpeedPass("S:1", discounted).getPrice());
        assertEquals(new BigDecimal("36.00"), new SpecialPass("P", discounted).getPrice());
        assertTrue(discounted.isActiveOn(DAY) && discounted.isValidOn(DAY) && !discounted.isDeletableOn(DAY));
        assertFalse(discounted.isActiveOn(DAY.plusDays(1)));
        assertEquals("S:3", SpeedPass.passId("S", 3));
    }
}
