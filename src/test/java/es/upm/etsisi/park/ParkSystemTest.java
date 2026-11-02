package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkSystemTest extends ParkTestSupport {

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> c : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()), "constructor must be private");
        }
        assertTrue(Modifier.isFinal(Park.class.getModifiers()));
    }

    @Test
    void startsWithGivenDateAndInitialPrice() {
        assertEquals(TODAY, park.getToday());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void systemDateCanChangeRepeatedly() {
        park.setToday("2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), park.getToday());
        park.setToday("2026-01-31");
        assertEquals(LocalDate.of(2026, 1, 31), park.getToday());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow", "2027-04-15T10:00"})
    void invalidDateRejected(String value) {
        assertThrows(ParkException.class, () -> park.setToday(value));
        assertEquals(TODAY, park.getToday());
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.50", "45.5", "45", "0.01", "100.00"})
    void validPriceAccepted(String value) {
        park.setBasePrice(value);
        assertEquals(new BigDecimal(value).setScale(2), park.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "4.555", "45,50", "", ".5", "5.", "1e3", "+5", " 5"})
    void invalidPriceRejected(String value) {
        assertThrows(ParkException.class, () -> park.setBasePrice(value));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void priceChangeOnlyAffectsNewTickets() {
        ana();
        IndividualTicket t1 = park.createTicket("T1", "ana@correo.com", TODAY_S);
        park.setBasePrice("45.50");
        IndividualTicket t2 = park.createTicket("T2", "ana@correo.com", TODAY_S);
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
    }

    @Test
    void priceAndDateResetOnNewExecution() {
        park.setBasePrice("45.50");
        park.setToday("2030-01-01");
        restart();
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertEquals(TODAY, park.getToday());
    }
}
