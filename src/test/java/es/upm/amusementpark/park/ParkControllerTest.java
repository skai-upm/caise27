package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.persistence.InMemoryUserStorage;

class ParkControllerTest {

    @Test
    void thereIsASingleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void noAccessibleConstructor() {
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void initializationStartsWithTodayAndInitialPrice() throws Exception {
        ParkController park = ParkController.getInstance();
        park.setBasePrice(new BigDecimal("99.00"));
        park.setCurrentDate(LocalDate.of(2000, 1, 1));
        park.initialize(new InMemoryUserStorage());
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }
}
