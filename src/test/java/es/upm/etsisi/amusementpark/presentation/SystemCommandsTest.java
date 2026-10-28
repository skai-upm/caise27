package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SystemCommandsTest {

    private final ParkController controller = ParkController.getInstance();

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @BeforeEach
    void knownState() {
        controller.setCurrentDate("2026-01-01");
        controller.setCurrentPrice("40.00");
    }

    @Test
    void systemDateSetsCurrentDate() throws IOException {
        String out = run("SYSTEM DATE;2027-04-15\n");
        assertTrue(out.contains("SYSTEM DATE UPDATED: 2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), controller.getCurrentDate());
    }

    @Test
    void systemDateCanChangeRepeatedly() throws IOException {
        run("SYSTEM DATE;2027-04-15\nSYSTEM DATE;2027-05-01\n");
        assertEquals(LocalDate.of(2027, 5, 1), controller.getCurrentDate());
    }

    @Test
    void invalidSystemDateRejected() throws IOException {
        String out = run("SYSTEM DATE;2027-02-30\nSYSTEM DATE;15/04/2027\nSYSTEM DATE\n");
        assertEquals(3, out.split("ERROR", -1).length - 1);
        assertEquals(LocalDate.of(2026, 1, 1), controller.getCurrentDate());
    }

    @Test
    void systemPriceSetsBasePrice() throws IOException {
        String out = run("SYSTEM PRICE;45.50\n");
        assertTrue(out.contains("SYSTEM PRICE UPDATED: 45.50"));
        assertEquals(new BigDecimal("45.50"), controller.getCurrentPrice());
    }

    @Test
    void invalidSystemPriceRejected() throws IOException {
        String out = run("SYSTEM PRICE;0\nSYSTEM PRICE;-5\nSYSTEM PRICE;abc\nSYSTEM PRICE;45.505\nSYSTEM PRICE\n");
        assertEquals(5, out.split("ERROR", -1).length - 1);
        assertEquals(new BigDecimal("40.00"), controller.getCurrentPrice());
    }
}
