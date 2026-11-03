package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkTest {

    @TempDir
    Path dir;

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> c : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
        assertTrue(Modifier.isFinal(Park.class.getModifiers()));
    }

    @Test
    void startResetsDateAndPrice() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run("SYSTEM PRICE;45.50", "SYSTEM DATE;2030-01-01");
        f.restart();
        assertEquals(Park.INITIAL_PRICE, f.park.getBasePrice());
        assertEquals(TestParks.TODAY, f.park.getCurrentDate());
    }
}
