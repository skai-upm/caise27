package es.upm.etsisi.park.core;

import es.upm.etsisi.park.service.SystemService;
import es.upm.etsisi.park.service.UserOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkTest {

    @TempDir
    Path dir;

    @Test
    void thereIsASingleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
    }

    @Test
    void loadResetsDateAndPrice() {
        Park park = Park.getInstance();
        park.load(dir);
        park.getSystemService().setDate("2030-01-01");
        park.getSystemService().setPrice("45.50");

        park.load(dir);

        assertEquals(LocalDate.now(ZoneId.systemDefault()), park.getState().getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getState().getBasePrice());
    }

    @Test
    void systemDateCanBeChangedRepeatedly() {
        Park park = Park.getInstance();
        park.load(dir);
        SystemService system = park.getSystemService();

        system.setDate("2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), park.getState().getCurrentDate());
        system.setDate("2026-01-01");
        assertEquals(LocalDate.of(2026, 1, 1), park.getState().getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", ""})
    void rejectsInvalidDates(String text) {
        Park park = Park.getInstance();
        park.load(dir);
        LocalDate before = park.getState().getCurrentDate();

        assertThrows(UserOperationException.class, () -> park.getSystemService().setDate(text));
        assertEquals(before, park.getState().getCurrentDate());
    }

    @Test
    void acceptsValidPrices() {
        Park park = Park.getInstance();
        park.load(dir);

        assertEquals(new BigDecimal("45.50"), park.getSystemService().setPrice("45.50"));
        assertEquals(new BigDecimal("45.50"), park.getSystemService().setPrice("45.5"));
        assertEquals(new BigDecimal("12.00"), park.getSystemService().setPrice("12"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-3", "abc", "10.999", ""})
    void rejectsInvalidPrices(String text) {
        Park park = Park.getInstance();
        park.load(dir);

        assertThrows(UserOperationException.class, () -> park.getSystemService().setPrice(text));
        assertEquals(new BigDecimal("40.00"), park.getState().getBasePrice());
    }
}
