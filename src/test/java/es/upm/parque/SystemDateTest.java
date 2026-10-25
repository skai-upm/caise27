package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemDateTest {

    @Test
    void parsesValidDate() {
        assertEquals(LocalDate.of(2026, 9, 15), SystemDate.parse("2026-09-15"));
        assertEquals("2026-09-15", SystemDate.format(LocalDate.of(2026, 9, 15)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15/09/2026", "2026-9-15", "hoy", "", "2026-02-30", "2026-13-01", "26-09-15"})
    void rejectsInvalidDates(String text) {
        assertNull(SystemDate.parse(text));
    }

    @Test
    void movesBackwardsAndForwards() {
        SystemDate date = new SystemDate(LocalDate.of(2026, 1, 1));
        date.set(LocalDate.of(2026, 9, 15));
        assertEquals(LocalDate.of(2026, 9, 15), date.get());
        date.set(LocalDate.of(2025, 1, 1));
        assertEquals(LocalDate.of(2025, 1, 1), date.get());
    }
}
