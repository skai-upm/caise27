package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.controller.TestUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com"));
        run("TICKET CREATE;T001;ana@correo.com;2027-04-16");
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void purchasesAndRefunds() {
        assertEquals("SPEEDPASS PURCHASED. TOTAL PRICE: 8.00", run("SPEEDPASS CREATE;SP01;T001;1"));
        assertEquals("SPEEDPASS PURCHASED. TOTAL PRICE: 80.00", run("SPEEDPASS CREATE;SP02;T001;10"));
        assertEquals("SPECIALPASS PURCHASED. PRICE: 40.00", run("SPECIALPASS CREATE;PE01;T001"));
        assertEquals("SPEEDPASS REFUNDED", run("SPEEDPASS DELETE;SP02:3"));
        assertEquals("SPECIALPASS REFUNDED", run("SPECIALPASS DELETE;PE01"));
    }

    @Test
    void errorsAreReported() {
        for (String line : new String[] {"SPEEDPASS CREATE;SP01;T001;2", "SPEEDPASS CREATE;SP01;T001;diez",
                "SPEEDPASS CREATE;SP01;T999;1", "SPEEDPASS CREATE;SP01;T001", "SPECIALPASS CREATE;PE01;T999",
                "SPEEDPASS DELETE;SP99:1", "SPECIALPASS DELETE;PE99", "SPEEDPASS DELETE"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
    }

    @Test
    void helpListsPassCommands() {
        String help = run("HELP");
        assertTrue(help.contains("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>"));
        assertTrue(help.contains("SPEEDPASS DELETE;<speedPassIdentifier>"));
        assertTrue(help.contains("SPECIALPASS CREATE;<identifier>;<individualTicket>"));
        assertTrue(help.contains("SPECIALPASS DELETE;<specialPassIdentifier>"));
    }
}
