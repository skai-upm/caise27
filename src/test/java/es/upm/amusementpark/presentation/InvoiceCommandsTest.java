package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.controller.ApplicationInitializer;

class InvoiceCommandsTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        ParkController park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444");
        processor.process("SYSTEM DATE;2027-04-10");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SYSTEM PRICE;45.50");
        processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-16");
    }

    @Test
    void userInvoiceListsDirectTicketsOnly() {
        String invoice = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertEquals(String.join("\n",
                "INVOICE (USER)",
                "Name: Ana Garcia Lopez",
                "DNI: 12345678Z",
                "Email: ana@correo.com",
                "[Individual tickets]",
                "  T001 (2027-04-15): 40.00 EUR",
                "  T002 (2027-04-16): 45.50 EUR",
                "TOTAL: 85.50 EUR"), invoice);
        assertFalse(invoice.contains("TG01"));
    }

    @Test
    void groupInvoiceShowsGroupTicketsWithComposition() {
        String invoice = processor.process("INVOICE CREATE;GROUP;G01");
        assertEquals(String.join("\n",
                "INVOICE (GROUP)",
                "Group: G01",
                "Name: Familia Garcia",
                "[Group tickets]",
                "  TG01 (2027-04-15, 3 members): 108.00 EUR",
                "    - TG01:12345678Z ana@correo.com: 36.00 EUR",
                "    - TG01:87654321X pedro@correo.com: 36.00 EUR",
                "    - TG01:11111111H luis@correo.com: 36.00 EUR",
                "TOTAL: 108.00 EUR"), invoice);
    }

    @Test
    void emptySectionsOmitted() {
        String invoice = processor.process("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("["), invoice);
        assertTrue(invoice.endsWith("TOTAL: 0.00 EUR"));
    }

    @Test
    void errors() {
        assertEquals("ERROR: User not found", processor.process("INVOICE CREATE;USER;nadie@correo.com"));
        assertEquals("ERROR: Group not found", processor.process("INVOICE CREATE;GROUP;G99"));
        assertTrue(processor.process("INVOICE CREATE;COMPANY;X").startsWith("ERROR: Invalid format"));
        assertTrue(processor.process("INVOICE CREATE;USER").startsWith("ERROR: Invalid format"));
    }

    @Test
    void helpListsInvoiceCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("INVOICE CREATE;USER;<email>"));
        assertTrue(help.contains("INVOICE CREATE;GROUP;<groupIdentifier>"));
    }
}
