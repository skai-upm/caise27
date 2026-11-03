package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemServiceTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
    }

    @Test
    void setsDate() {
        assertEquals("SYSTEM DATE SET TO 2027-05-01", f.run("SYSTEM DATE;2027-05-01"));
        assertEquals(LocalDate.of(2027, 5, 1), f.park.getCurrentDate());
        f.run("SYSTEM DATE;2026-01-01");
        assertEquals(LocalDate.of(2026, 1, 1), f.park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-13-45", "15/04/2027", "2027-4-15", "", "tomorrow"})
    void rejectsInvalidDate(String date) {
        assertTrue(f.run("SYSTEM DATE;" + date).startsWith("ERROR: invalid date"));
        assertEquals(TestParks.TODAY, f.park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"45.50", "45.5", "45", "0.01", "100.00"})
    void setsPrice(String price) {
        assertTrue(f.run("SYSTEM PRICE;" + price).startsWith("SYSTEM PRICE SET TO"));
        assertEquals(new BigDecimal(price).setScale(2), f.park.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "10.999", "", ".5", "5.", "1e3", "4,50", " 45"})
    void rejectsInvalidPrice(String price) {
        assertTrue(f.run("SYSTEM PRICE;" + price).startsWith("ERROR: invalid price"));
        assertEquals(Park.INITIAL_PRICE, f.park.getBasePrice());
    }
}
