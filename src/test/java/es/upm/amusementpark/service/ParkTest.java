package es.upm.amusementpark.service;

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

class ParkTest extends ParkTestSupport {

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> constructor : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
        assertTrue(Modifier.isFinal(Park.class.getModifiers()));
    }

    @Test
    void defaultsAtStart() throws Exception {
        park.setCurrentDate("2030-01-01");
        park.setCurrentPrice("45.50");
        restart();
        park.initialize(storage(), TODAY);
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void setDate() throws Exception {
        park.setCurrentDate("2027-04-20");
        assertEquals(LocalDate.of(2027, 4, 20), park.getCurrentDate());
        park.setCurrentDate("2027-01-01");
        assertEquals(LocalDate.of(2027, 1, 1), park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow"})
    void invalidDateKeepsCurrent(String value) {
        assertThrows(OperationException.class, () -> park.setCurrentDate(value));
        assertEquals(TODAY, park.getCurrentDate());
    }

    @Test
    void setPrice() throws Exception {
        park.setCurrentPrice("45.50");
        assertEquals(new BigDecimal("45.50"), park.getCurrentPrice());
        park.setCurrentPrice("45");
        assertEquals(new BigDecimal("45.00"), park.getCurrentPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "1e2", "10.123", "", ".5", "5.", "1,50"})
    void invalidPriceKeepsCurrent(String value) {
        assertThrows(OperationException.class, () -> park.setCurrentPrice(value));
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }
}
