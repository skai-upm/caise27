package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerTest {

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.initialize(new UserRepository(), LocalDate.of(2026, 10, 27));
    }

    @Test
    void sameInstanceEverywhere() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void noPublicConstructor() {
        Constructor<?>[] constructors = ParkController.class.getDeclaredConstructors();
        assertTrue(Arrays.stream(constructors).allMatch(c -> Modifier.isPrivate(c.getModifiers())));
    }

    @Test
    void initialStateUsesGivenDateAndDefaultPrice() {
        assertEquals(LocalDate.of(2026, 10, 27), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void dateAndPriceCanBeReplaced() {
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        park.setCurrentPrice(new BigDecimal("45.5"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertEquals(new BigDecimal("45.50"), park.getCurrentPrice());
    }

    @Test
    void nonPositivePriceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(new BigDecimal("-5")));
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }
}
