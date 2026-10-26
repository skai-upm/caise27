package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SystemDateTest {

    @Test
    void startsAtOperatingSystemDate() {
        assertEquals(LocalDate.now(), SystemDate.today().get());
    }

    @Test
    void parsesValidDates() {
        assertEquals(LocalDate.of(2026, 9, 15), SystemDate.parse("2026-09-15"));
        assertEquals(LocalDate.of(2028, 2, 29), SystemDate.parse("2028-02-29"));
    }

    @Test
    void rejectsInvalidDates() {
        assertThrows(IllegalArgumentException.class, () -> SystemDate.parse("15/09/2026"));
        assertThrows(IllegalArgumentException.class, () -> SystemDate.parse("2026-02-30"));
        assertThrows(IllegalArgumentException.class, () -> SystemDate.parse("2026-9-15"));
        assertThrows(IllegalArgumentException.class, () -> SystemDate.parse(""));
    }

    @Test
    void setChangesTheSharedDate() {
        SystemDate date = new SystemDate(LocalDate.of(2026, 1, 1));
        date.set(LocalDate.of(2025, 12, 31));
        assertEquals(LocalDate.of(2025, 12, 31), date.get());
    }
}
