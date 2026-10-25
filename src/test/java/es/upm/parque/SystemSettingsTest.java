package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

import es.upm.parque.business.validation.Identifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemSettingsTest {

    @TempDir
    Path dir;

    @Test
    void systemDateStartsWithTheOperatingSystemDate() {
        App.run(new String[0], dir, dir.resolve("tmp"), new java.io.ByteArrayInputStream(new byte[0]),
                new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
        assertEquals(LocalDate.now(ZoneId.systemDefault()),
                es.upm.parque.business.controller.ParkController.getInstance().getSystemDate());
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        ParkTestSupport t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 16));
        assertEquals("OK SYSTEM DATE -> 2026-09-15", t.run("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), t.park.getSystemDate());
        t.run("SYSTEM DATE;2027-01-01");
        assertEquals(LocalDate.of(2027, 1, 1), t.park.getSystemDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"15/09/2026", "2026-9-15", "2026-02-30", "", "hoy"})
    void invalidDatesAreRejected(String date) {
        ParkTestSupport t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 16));
        assertTrue(t.run("SYSTEM DATE;" + date).startsWith("ERROR"));
        assertEquals(LocalDate.of(2026, 10, 16), t.park.getSystemDate());
    }

    @Test
    void systemDateIsNotPersisted() {
        ParkTestSupport t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 16));
        t.run("SYSTEM DATE;2030-01-01");
        assertEquals(LocalDate.of(2026, 10, 16), t.restart(LocalDate.of(2026, 10, 16)).park.getSystemDate());
    }

    @Test
    void ticketPriceDefaultsTo40AndCanChange() {
        ParkTestSupport t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 1));
        assertEquals(new BigDecimal("40.00"), t.park.getTicketPrice());
        assertEquals("OK SYSTEM PRICE -> 60.00", t.run("SYSTEM PRICE;60.00"));
        assertEquals("OK SYSTEM PRICE -> 55.50", t.run("SYSTEM PRICE;55.5"));
        assertEquals(new BigDecimal("40.00"), t.restart(LocalDate.of(2026, 10, 1)).park.getTicketPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "0", "abc", ""})
    void invalidPricesAreRejected(String price) {
        ParkTestSupport t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 1));
        assertTrue(t.run("SYSTEM PRICE;" + price).startsWith("ERROR"));
        assertEquals(new BigDecimal("40.00"), t.park.getTicketPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"G01", "a-b_c", "T001"})
    void validIdentifiers(String id) {
        assertTrue(Identifiers.isValid(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "G 01", "G;01", "G,01", "G:01"})
    void invalidIdentifiers(String id) {
        assertFalse(Identifiers.isValid(id));
    }

    @Test
    void nullIdentifierIsInvalid() {
        assertFalse(Identifiers.isValid(null));
    }
}
