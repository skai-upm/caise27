package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerTest {

    @BeforeEach
    void reset() {
        ParkController.getInstance().reset();
    }

    @Test
    void sameInstanceEverywhere() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void constructorsArePrivate() {
        for (Constructor<?> constructor : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Test
    void initialStateIsTodayAnd40() {
        assertEquals(LocalDate.now(), ParkController.getInstance().getCurrentDate());
        assertEquals(new BigDecimal("40.00"), ParkController.getInstance().getCurrentPrice());
    }

    @Test
    void resetRestoresInitialPrice() {
        ParkController park = ParkController.getInstance();
        park.setCurrentPrice(new BigDecimal("45.50"));
        park.reset();
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void nonPositivePriceRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ParkController.getInstance().setCurrentPrice(BigDecimal.ZERO));
    }
}
