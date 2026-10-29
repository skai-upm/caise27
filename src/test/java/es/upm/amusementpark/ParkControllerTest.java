package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkControllerTest {

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void resetState() {
        park.reset();
    }

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void noPublicConstructor() {
        for (Constructor<?> constructor : ParkController.class.getDeclaredConstructors()) {
            assertEquals(true, Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Test
    void defaultsAreTodayAndFortyEuros() {
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void setValidDate() {
        park.setCurrentDate("2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15-04-2027", "2027-4-15", "", "abc"})
    void invalidDatesRejected(String value) {
        LocalDate before = park.getCurrentDate();
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentDate(value));
        assertEquals(before, park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.50", "45", "0.01", "45.5"})
    void setValidPrice(String value) {
        park.setBasePrice(value);
        assertEquals(new BigDecimal(value).setScale(2), park.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-3", "abc", "45,50", "1.234", "", ".", "4.", ".5", "1e2", "+5"})
    void invalidPricesRejected(String value) {
        assertThrows(IllegalArgumentException.class, () -> park.setBasePrice(value));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }
}
