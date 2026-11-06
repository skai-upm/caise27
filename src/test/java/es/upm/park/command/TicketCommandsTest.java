package es.upm.park.command;

import es.upm.park.controller.ParkController;
import es.upm.park.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        ParkController.getInstance().setUserExistsLookup(Set.of("ana@correo.com")::contains);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createDisplaysPriceAndDeleteFutureTicket() {
        assertEquals("TICKET CREATED. PRICE: 40.00", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        run("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. PRICE: 45.50", run("TICKET CREATE;T002;ana@correo.com;2027-04-16"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
    }

    @Test
    void invalidArgumentsAreRejected() {
        for (String line : new String[] {"TICKET CREATE;T001;ana@correo.com;2027-04-14",
                "TICKET CREATE;T001;ana@correo.com;2027-02-30", "TICKET CREATE;T001;ana@correo.com;15/04/2027",
                "TICKET CREATE;T001;nadie@correo.com;2027-04-15", "TICKET CREATE;T001;ana@correo.com",
                "TICKET DELETE", "TICKET DELETE;T999"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
        assertTrue(ParkController.getInstance().findTicket("T001").isEmpty());
    }

    @Test
    void helpListsTicketCommands() {
        String help = run("HELP");
        assertTrue(help.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(help.contains("TICKET DELETE;<identifier>"));
    }
}
