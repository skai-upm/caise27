package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.persistence.repository.UserRepository;

class ParkControllerTest {

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void startResetsDateAndPrice() throws Exception {
        ParkController park = ParkController.getInstance();
        park.setCurrentDate(LocalDate.of(2030, 1, 1));
        park.setCurrentPrice(new BigDecimal("99.99"));
        UserRepository users = new UserRepository();
        ApplicationInitializer.startInMemory(users);
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
        assertSame(users, park.getUsers());
    }
}
