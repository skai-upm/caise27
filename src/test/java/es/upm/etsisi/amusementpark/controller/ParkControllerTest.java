package es.upm.etsisi.amusementpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkControllerTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void resetState() {
        controller.reset();
    }

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void initialState() {
        assertEquals(LocalDate.now(), controller.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), controller.getCurrentPrice());
    }

    @Test
    void dateCanBeChangedRepeatedly() {
        controller.setCurrentDate("2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), controller.getCurrentDate());
        controller.setCurrentDate("2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), controller.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "+10000-01-01"})
    void invalidDateRejectedAndUnchanged(String value) {
        controller.setCurrentDate("2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> controller.setCurrentDate(value));
        assertEquals(LocalDate.of(2027, 4, 15), controller.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.50", "45.5", "45"})
    void validPrices(String value) {
        controller.setCurrentPrice(value);
        assertEquals(0, new BigDecimal(value).compareTo(controller.getCurrentPrice()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "45.505", "45,50", "1e2", ".5", "5.", ""})
    void invalidPriceRejectedAndUnchanged(String value) {
        assertThrows(IllegalArgumentException.class, () -> controller.setCurrentPrice(value));
        assertEquals(new BigDecimal("40.00"), controller.getCurrentPrice());
    }
}
