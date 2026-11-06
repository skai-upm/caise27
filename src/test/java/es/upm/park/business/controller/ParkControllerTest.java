package es.upm.park.business.controller;

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

import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.service.ParkException;

class ParkControllerTest {

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.start(new UserRepository(), LocalDate.of(2026, 1, 2));
    }

    @Test
    void alwaysTheSameInstance() {
        assertSame(park, ParkController.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void cannotBeInstantiatedFromOutside() {
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void startResetsDateAndPrice() {
        park.setBasePrice("45.50");
        park.setCurrentDate("2027-04-15");
        UserRepository users = new UserRepository();
        park.start(users, LocalDate.of(2026, 3, 4));
        assertEquals(LocalDate.of(2026, 3, 4), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void rejectsInvalidValuesWithoutChange() {
        assertThrows(ParkException.class, () -> park.setCurrentDate(null));
        assertThrows(ParkException.class, () -> park.setBasePrice(null));
        assertThrows(ParkException.class, () -> park.setBasePrice("00"));
        assertEquals(LocalDate.of(2026, 1, 2), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }
}
