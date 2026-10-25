package com.amusementpark.business.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ValidatorParsingTest {

    @ParameterizedTest
    @ValueSource(strings = {"G01", "T001", "TG01-1", "a_b"})
    public void validIds(String id) {
        assertTrue(Validator.isValidId(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "G 01", "G,01", "G:01", "G;01"})
    public void invalidIds(String id) {
        assertFalse(Validator.isValidId(id));
    }

    @Test
    public void parsesStrictDates() {
        assertEquals(LocalDate.of(2026, 9, 15), Validator.parseDate("2026-09-15"));
        assertEquals(LocalDate.of(2028, 2, 29), Validator.parseDate("2028-02-29"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15-09-2026", "2026-9-15", "2026-02-30", "2026-13-01", "hoy", "", "2026/09/15"})
    public void rejectsInvalidDates(String text) {
        assertNull(Validator.parseDate(text));
    }

    @Test
    public void parsesPrices() {
        assertEquals(new BigDecimal("60.00"), Validator.parsePrice("60.00"));
        assertEquals(new BigDecimal("45"), Validator.parsePrice("45"));
        assertEquals(new BigDecimal("0.5"), Validator.parsePrice("0.5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "1.234", "1.", ".5", "1e3", ""})
    public void rejectsInvalidPrices(String text) {
        assertNull(Validator.parsePrice(text));
    }

    @Test
    public void parsesCapacity() {
        assertEquals(20, Validator.parseCapacity("20"));
        assertEquals(-1, Validator.parseCapacity("0"));
        assertEquals(-1, Validator.parseCapacity("-3"));
        assertEquals(-1, Validator.parseCapacity("diez"));
        assertEquals(-1, Validator.parseCapacity("99999999999"));
    }
}
