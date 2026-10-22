package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.ParkStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkTest {

    @TempDir
    Path dir;

    private ParkStore store(Path directory) {
        return new ParkStore(directory, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    @Test
    void thereIsExactlyOneInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> constructor : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
        assertTrue(Modifier.isFinal(Park.class.getModifiers()));
    }

    @Test
    void openSetsInitialDateAndPrice() throws IOException {
        Park park = Park.getInstance();
        LocalDate date = LocalDate.of(2027, 4, 15);
        park.open(store(dir), date);
        assertEquals(date, park.getDate());
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void openResetsStateFromTheGivenStore() throws IOException, ParkException {
        Park park = Park.getInstance();
        park.open(store(dir.resolve("a")), LocalDate.of(2027, 4, 15));
        park.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertNotNull(park.findUserByEmail("ana@correo.com"));

        park.open(store(dir.resolve("b")), LocalDate.of(2027, 4, 15));
        assertNull(park.findUserByEmail("ana@correo.com"));

        park.open(store(dir.resolve("a")), LocalDate.of(2027, 4, 15));
        assertNotNull(park.findUserByEmail("ana@correo.com"));
    }
}
