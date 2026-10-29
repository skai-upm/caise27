package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceTest extends ParkFixture {

    InvoiceController invoices;

    @BeforeEach
    void purchases() throws Exception {
        invoices = InvoiceController.getInstance();
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        park.createTicket("T001", ANA, "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.buySpeedPasses("SP01", "T001", "1");
        park.setCurrentPrice("45");
        park.buySpecialPass("PE01", "TG01:12345678Z");
    }

    private static List<String> titles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void userInvoiceWithTicketsAndPasses() throws Exception {
        Invoice invoice = invoices.userInvoice(ANA);
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), titles(invoice));
        assertEquals(1, invoice.getSections().get(0).lines().size());
        assertTrue(invoice.getSections().get(0).lines().get(0).description().contains("T001"));
        // T001 40.00 + SpeedPass 8.00 + Special Pass on composition ticket 40.00 (stored price, not 45)
        assertEquals(new BigDecimal("88.00"), invoice.getTotal());
        assertTrue(invoice.getHeader().contains("DNI: 12345678Z"));
    }

    @Test
    void userInvoiceOmitsEmptySectionsAndCompositionTickets() throws Exception {
        Invoice invoice = invoices.userInvoice(PEDRO);
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        String text = processor().process("INVOICE CREATE;USER;" + PEDRO);
        assertFalse(text.contains("TG01"));
        assertFalse(text.contains("--"));
        assertTrue(text.contains("TOTAL: 0.00 EUR"));
    }

    @Test
    void groupInvoiceCountsGroupTicketsOnly() throws Exception {
        Invoice invoice = invoices.groupInvoice("G01");
        assertEquals(List.of("Group tickets"), titles(invoice));
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        assertEquals(2, invoice.getSections().get(0).lines().get(0).details().size());
        String text = processor().process("INVOICE CREATE;GROUP;G01");
        assertTrue(text.contains("Group: G01"));
        assertTrue(text.contains("Name: Familia Garcia"));
        assertTrue(text.contains("TG01:12345678Z"));
        assertTrue(text.contains("TG01:87654321X"));
        assertFalse(text.contains("PE01"));
        assertTrue(text.contains("TOTAL: 80.00 EUR"));
    }

    @Test
    void invalidRequests() throws Exception {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
        CommandProcessor processor = processor();
        assertTrue(processor.process("INVOICE CREATE;COMPANY;X").startsWith("ERROR:"));
        assertTrue(processor.process("INVOICE CREATE;USER").startsWith("ERROR:"));
        park.setCurrentDate("2027-04-16");
        park.deleteUser(PEDRO, "Otra2#");
        assertThrows(NotFoundException.class, () -> invoices.userInvoice(PEDRO));
    }

    @Test
    void buildersUseStepsNotTelescopingConstructors() {
        assertTrue(Modifier.isAbstract(InvoiceBuilder.class.getModifiers()));
        assertEquals(InvoiceBuilder.class, UserInvoiceBuilder.class.getSuperclass());
        assertEquals(InvoiceBuilder.class, GroupInvoiceBuilder.class.getSuperclass());
        for (Constructor<?> c : Invoice.class.getDeclaredConstructors()) {
            assertFalse(Modifier.isPublic(c.getModifiers()));
        }
        Invoice empty = new GroupInvoiceBuilder().build();
        assertTrue(empty.getSections().isEmpty());
    }
}
