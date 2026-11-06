package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.controller.TestUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com"));
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void userAndGroupInvoices() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        String user = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(user.startsWith("INVOICE - USER"));
        assertTrue(user.contains("T001 (2027-04-15) 40.00"));
        assertTrue(user.endsWith("TOTAL: 40.00"));
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        String group = run("INVOICE CREATE;GROUP;G01");
        assertTrue(group.startsWith("INVOICE - GROUP"));
        assertTrue(group.contains("TG01:DNI1 ana@correo.com (included)"));
        assertTrue(group.endsWith("TOTAL: 40.00"));
    }

    @Test
    void invalidRequestsAreRejected() {
        for (String line : new String[] {"INVOICE CREATE;COMPANY;X", "INVOICE CREATE;USER",
                "INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
    }

    @Test
    void helpListsInvoiceCommands() {
        String help = run("HELP");
        assertTrue(help.contains("INVOICE CREATE;USER;<email>"));
        assertTrue(help.contains("INVOICE CREATE;GROUP;<groupIdentifier>"));
    }
}
