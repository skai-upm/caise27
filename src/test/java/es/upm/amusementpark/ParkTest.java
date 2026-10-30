package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkTest extends ParkTestSupport {

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
    }

    @Test
    void startsWithTodayAndFortyEuros() {
        system.setDate("2030-01-01");
        system.setPrice("99");
        restart();
        assertEquals(LocalDate.now(), park.getDate());
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void dateCanChangeRepeatedly() {
        system.setDate("2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), park.getDate());
        system.setDate("2026-01-31");
        assertEquals(LocalDate.of(2026, 1, 31), park.getDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "tomorrow", "2027-04-15x"})
    void invalidDatesRejected(String text) {
        LocalDate before = park.getDate();
        assertThrows(IllegalArgumentException.class, () -> system.setDate(text));
        assertEquals(before, park.getDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.50", "45", "0.5", "100.00"})
    void validPrices(String text) {
        system.setPrice(text);
        assertEquals(new BigDecimal(text).setScale(2), park.getPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "4.555", "4.", ".5", "1e3", "+5", "4,50"})
    void invalidPricesRejected(String text) {
        assertThrows(IllegalArgumentException.class, () -> system.setPrice(text));
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }
}
