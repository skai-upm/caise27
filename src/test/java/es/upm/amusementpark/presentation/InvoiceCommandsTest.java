package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceCommandsTest {

    private static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(TestParks.open(dir));
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void userInvoiceListsOnlyDirectTickets() {
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertEquals(String.join(NL,
                "=== USER INVOICE ===",
                "Date: 2027-04-15",
                "Customer: Ana Garcia Lopez",
                "DNI: 12345678Z",
                "Email: ana@correo.com",
                "Individual tickets:",
                "  Ticket T001 for 2027-04-15 ... 40.00 EUR",
                "TOTAL: 40.00 EUR"), invoice);
        assertFalse(invoice.contains("TG01"));
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        String invoice = run("invoice create;user;PEDRO@correo.com");
        assertFalse(invoice.contains("Individual tickets"));
        assertTrue(invoice.endsWith("TOTAL: 0.00 EUR"));
    }

    @Test
    void groupInvoiceListsGroupTicketsWithComposition() {
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("=== GROUP INVOICE ===" + NL + "Date: 2027-04-15" + NL
                + "Group: G01 - Familia Garcia" + NL + "Group tickets:"));
        assertTrue(invoice.contains("Group ticket TG01 for 2027-04-15, 3 tickets, 10% discount ... 108.00 EUR"));
        assertTrue(invoice.contains("TG01:87654321X Pedro Lopez Martin (36.00 EUR)"));
        assertTrue(invoice.endsWith("TOTAL: 108.00 EUR"));
        assertFalse(invoice.contains("T001"));
    }

    @Test
    void invalidRequestsAreRejected() {
        assertTrue(run("INVOICE CREATE;COMPANY;X").startsWith("ERROR"));
        assertTrue(run("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(run("INVOICE CREATE;USER;nobody@correo.com").startsWith("ERROR"));
        String usage = run("INVOICE CREATE;USER");
        assertTrue(usage.contains("INVOICE CREATE;USER;<email> or INVOICE CREATE;GROUP;<groupIdentifier>"));
    }
}
