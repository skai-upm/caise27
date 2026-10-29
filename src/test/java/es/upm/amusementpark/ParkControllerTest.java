package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkControllerTest extends ParkTestSupport {

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertEquals(true, Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void initialDateAndPrice() {
        park.initialize(dir);
        assertEquals(LocalDate.now(ZoneId.systemDefault()), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void priceResetsOnNewExecution() {
        park.setPrice(new BigDecimal("45.50"));
        park.initialize(dir);
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void validDatesAndPrices() {
        assertEquals(LocalDate.of(2027, 4, 15), ValueParser.parseDate("2027-04-15"));
        assertEquals(new BigDecimal("45.50"), ValueParser.parsePrice("45.50"));
        assertEquals(new BigDecimal("45.00"), ValueParser.parsePrice("45"));
        assertEquals(new BigDecimal("0.50"), ValueParser.parsePrice("0.5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "abc"})
    void invalidDates(String value) {
        assertThrows(IllegalArgumentException.class, () -> ValueParser.parseDate(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-3", "abc", "1.234", "", ".5", "5.", "1,5", "1e3"})
    void invalidPrices(String value) {
        assertThrows(IllegalArgumentException.class, () -> ValueParser.parsePrice(value));
    }

    @Test
    void existingTicketKeepsItsPrice() {
        user(1);
        Ticket t = park.createTicket("T001", email(1), "2027-04-15");
        park.setPrice(new BigDecimal("45.50"));
        assertEquals(new BigDecimal("40.00"), t.getPrice());
        assertEquals(new BigDecimal("45.50"), park.createTicket("T002", email(1), "2027-04-15").getPrice());
    }
}
