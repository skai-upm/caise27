package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemCommandsTest {

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(out);
        SystemCommands.register(processor, park, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void validDateReplacesCurrentDate() {
        processor.execute("SYSTEM DATE;2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertTrue(output().contains("SYSTEM DATE UPDATED: 2027-04-15"));
    }

    @Test
    void dateCanChangeSeveralTimes() {
        processor.execute("SYSTEM DATE;2027-04-15");
        processor.execute("SYSTEM DATE;2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027", "SYSTEM DATE", "SYSTEM DATE;",
        "SYSTEM DATE;2027-4-15", "SYSTEM DATE;2027-04-15;x"})
    void invalidDateRejectedAndStateUnchanged(String command) {
        processor.execute("SYSTEM DATE;2027-04-15");
        processor.execute(command);
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertTrue(output().contains("ERROR: Invalid date"));
    }

    @Test
    void validPriceReplacesBasePrice() {
        processor.execute("SYSTEM PRICE;45.50");
        assertEquals(new BigDecimal("45.50"), park.getCurrentPrice());
        assertTrue(output().contains("SYSTEM PRICE UPDATED: 45.50"));
    }

    @Test
    void integerPriceAccepted() {
        processor.execute("SYSTEM PRICE;45");
        assertEquals(new BigDecimal("45.00"), park.getCurrentPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;0", "SYSTEM PRICE;-3", "SYSTEM PRICE;abc", "SYSTEM PRICE;10.999",
        "SYSTEM PRICE", "SYSTEM PRICE;", "SYSTEM PRICE;1e3", "SYSTEM PRICE;.5", "SYSTEM PRICE;5.", "SYSTEM PRICE;0.00"})
    void invalidPriceRejectedAndStateUnchanged(String command) {
        processor.execute("SYSTEM PRICE;45.50");
        processor.execute(command);
        assertEquals(new BigDecimal("45.50"), park.getCurrentPrice());
        assertTrue(output().contains("ERROR: Invalid price"));
    }

    @Test
    void helpListsSystemCommands() {
        processor.execute("HELP");
        assertTrue(output().contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(output().contains("SYSTEM PRICE;<Price>"));
    }
}
