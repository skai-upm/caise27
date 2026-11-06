package es.upm.park.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ParkControllerTest {

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void reset() {
        park.reset();
    }

    @Test
    void everyRequestReturnsTheSameInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        assertEquals(LocalDate.of(2027, 4, 15), ParkController.getInstance().getCurrentDate());
    }

    @Test
    void initialStateIsLocalDateAndFortyEuros() {
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void invalidPricesAreRejectedWithoutChangingState() {
        for (String value : new String[] {"0", "-5", "10.999"}) {
            assertThrows(ParkException.class, () -> park.setBasePrice(new BigDecimal(value)));
        }
        assertThrows(ParkException.class, () -> park.setBasePrice(null));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void nullDateIsRejected() {
        LocalDate before = park.getCurrentDate();
        assertThrows(ParkException.class, () -> park.setCurrentDate(null));
        assertEquals(before, park.getCurrentDate());
    }

    @Test
    void priceIsNormalisedToTwoDecimals() {
        park.setBasePrice(new BigDecimal("45.5"));
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
    }
}
