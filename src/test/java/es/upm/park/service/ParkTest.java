package es.upm.park.service;

import es.upm.park.persistence.DataPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkTest {

    @TempDir
    Path dir;

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> constructor : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Test
    void openResetsDateAndPrice() {
        Park park = Park.getInstance();
        park.open(new DataPaths(dir));
        park.setCurrentDate(LocalDate.of(2030, 1, 1));
        park.setCurrentPrice(new BigDecimal("45.50"));

        park.open(new DataPaths(dir));

        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }
}
