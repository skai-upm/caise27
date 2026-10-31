package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.StoragePaths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemSettingsTest extends ParkTestSupport {

    @Test
    void setsValidDateForwardAndBackward() {
        assertTrue(ok(run("SYSTEM DATE;2026-09-15")));
        assertEquals(LocalDate.of(2026, 9, 15), park.getSystemDate());
        assertTrue(ok(run("SYSTEM DATE;2026-10-16")));
        assertTrue(ok(run("SYSTEM DATE;2026-10-01")));
        assertEquals(LocalDate.of(2026, 10, 1), park.getSystemDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;15/09/2026", "SYSTEM DATE;2026-02-30", "SYSTEM DATE;",
            "SYSTEM DATE;2026-9-15", "SYSTEM DATE"})
    void rejectsInvalidDates(String command) {
        run("SYSTEM DATE;2026-09-15");
        assertTrue(error(run(command)));
        assertEquals(LocalDate.of(2026, 9, 15), park.getSystemDate());
    }

    @Test
    void dateIsNotPersisted() throws IOException {
        run("SYSTEM DATE;2030-01-01");
        restart();
        assertEquals(LocalDate.of(2026, 10, 1), park.getSystemDate());
    }

    @Test
    void appStartsWithTodaysDate() throws IOException {
        ApplicationContext.stop();
        ParkController fresh = ApplicationContext.start(new StoragePaths(dir), LocalDate.now());
        assertEquals(LocalDate.now(), fresh.getSystemDate());
    }

    @Test
    void priceStartsAt40AndCanChange() {
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertTrue(ok(run("SYSTEM PRICE;60.00")));
        assertEquals(new BigDecimal("60.00"), park.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;abc", "SYSTEM PRICE;-5", "SYSTEM PRICE;"})
    void rejectsInvalidPrice(String command) {
        assertTrue(error(run(command)));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void priceResetsOnRestart() throws IOException {
        run("SYSTEM PRICE;60.00");
        restart();
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String help = run("HELP EXTENDED");
        assertTrue(help.contains("INVOICE CREATE;USER;Correo"));
        assertTrue(help.contains("Sets the simulated current date used by every date rule."));
        assertTrue(error(run("HELP EXTENDED;x")));
    }

    @Test
    void helpListsEveryCommand() {
        String help = run("HELP");
        for (String command : new String[] {"USER CREATE;", "USER DELETE;", "USER UPDATE;",
                "SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;", "GROUP CREATE;", "GROUP ADD;", "GROUP REMOVE;",
                "GROUP DELETE;", "TICKET CREATE;", "TICKET DELETE;", "GROUPTICKET CREATE;",
                "GROUPTICKET DELETE;", "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]", "ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]", "ATTRACTION EXIT;",
                "ATTRACTION STATUS;", "SPEEDPASS CREATE;", "SPEEDPASS DELETE;", "SPECIALPASS CREATE;", "SPECIALPASS DELETE;", "INVOICE CREATE;USER;", "INVOICE CREATE;GROUP;", "HELP", "HELP EXTENDED", "EXIT"}) {
            assertTrue(help.contains(command), command);
        }
    }
}
