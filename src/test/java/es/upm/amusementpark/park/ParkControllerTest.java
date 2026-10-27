package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.repository.UserRepository;

class ParkControllerTest {

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void startResetsDateAndPrice() {
        ParkController park = ParkController.getInstance();
        park.setCurrentDate(LocalDate.of(2030, 1, 1));
        park.setCurrentPrice(new BigDecimal("99.99"));
        UserRepository users = new UserRepository();
        park.start(users);
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
        assertSame(users, park.getUsers());
    }
}
