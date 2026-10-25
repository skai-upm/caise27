package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.InvoiceLine;
import es.upm.etsisi.park.business.invoice.InvoiceSection;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceControllerTest extends Delivery3ScenarioBase {

    private static List<String> titles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void singleton() {
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void anasInvoice() {
        park.buySpeedPasses("SP01", "T001", "10");
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.buySpeedPasses("SP03", "TG01:87654321X", "1");

        Invoice invoice = park.createInvoice("USER", "ana@correo.com");

        assertEquals(new BigDecimal("167.20"), invoice.getTotal());
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), titles(invoice));
        assertEquals("12345678Z", invoice.getRecipient().get("DNI"));
        String text = invoice.toText();
        assertTrue(text.contains("Ticket T001"));
        assertTrue(text.contains("SP02:1 for ticket TG01:12345678Z"));
        assertFalse(text.contains("SP03:1"));
        assertFalse(text.contains("Ticket TG01"));
        assertTrue(text.contains("TOTAL: 167.20 EUR"));
    }

    @Test
    void groupInvoiceDoesNotDoubleCountComponentsOrPasses() {
        park.buySpeedPasses("SP02", "TG01:12345678Z", "1");
        Invoice invoice = park.createInvoice("group", "G01");
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertEquals(List.of("Group tickets"), titles(invoice));
        List<InvoiceLine> lines = invoice.getSections().get(0).lines();
        assertEquals(1, lines.stream().filter(InvoiceLine::isPriced).count());
        assertEquals(3, lines.stream().filter(line -> !line.isPriced()).count());
        String text = invoice.toText();
        assertTrue(text.contains("G01"));
        assertTrue(text.contains("Familia Garcia"));
        assertTrue(text.contains("TG01:11111111H"));
        assertFalse(text.contains("SP02"));
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice pedro = park.createInvoice("USER", "pedro@correo.com");
        assertTrue(pedro.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), pedro.getTotal());
        park.createTicket("T005", "pedro@correo.com", FUTURE);
        assertEquals(List.of("Individual tickets"), titles(park.createInvoice("USER", "pedro@correo.com")));
        park.createGroup("G02", "Vacio");
        assertTrue(park.createInvoice("GROUP", "G02").getSections().isEmpty());
    }

    @Test
    void unknownTargets() {
        assertThrows(NotFoundException.class, () -> park.createInvoice("USER", "nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> park.createInvoice("GROUP", "G99"));
        assertThrows(ValidationException.class, () -> park.createInvoice("COMPANY", "X"));
    }
}
