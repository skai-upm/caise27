package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceControllerTest extends ParkFixture {

    private InvoiceController invoices;
    private TicketController tickets;
    private PassController passes;

    @BeforeEach
    void setUp() {
        invoices = InvoiceController.getInstance();
        tickets = TicketController.getInstance();
        passes = PassController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    private static List<String> titles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void userWithTicketAndPasses() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpecialPass("PE01", "T001");

        Invoice invoice = invoices.create("USER", "ana@correo.com");

        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), titles(invoice));
        assertEquals(new BigDecimal("88.00"), invoice.getTotal());
        String text = invoice.toText();
        assertTrue(text.contains("DNI: 12345678Z"));
        assertTrue(text.contains("Ticket T001"));
        assertTrue(text.contains("TOTAL: 88.00 EUR"));
    }

    @Test
    void compositionTicketsNotChargedToUserButTheirPassesAre() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        passes.createSpeedPasses("SPP", "TG01:87654321X", "1");

        Invoice invoice = invoices.userInvoice("pedro@correo.com");

        assertEquals(List.of("SpeedPasses"), titles(invoice));
        assertEquals(new BigDecimal("8.00"), invoice.getTotal());
        assertFalse(invoice.toText().contains("Ticket TG01"));
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        Invoice invoice = invoices.userInvoice("ana@correo.com");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        assertTrue(invoice.toText().contains("TOTAL: 0.00 EUR"));
    }

    @Test
    void groupInvoiceChargesGroupTicketsOnly() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        passes.createSpeedPasses("SPA", "TG01:12345678Z", "10");
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());

        Invoice invoice = invoices.create("GROUP", "G01");

        assertEquals(List.of("Group tickets", "Composition of TG01"), titles(invoice));
        assertTrue(invoice.getSections().get(0).charged());
        assertFalse(invoice.getSections().get(1).charged());
        assertEquals(2, invoice.getSections().get(1).items().size());
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        String text = invoice.toText();
        assertTrue(text.contains("Group: G01"));
        assertTrue(text.contains("Name: Familia Garcia"));
        assertTrue(text.contains("TG01:12345678Z"));
        assertTrue(text.contains("TOTAL: 80.00 EUR"));
    }

    @Test
    void groupWithoutTickets() {
        Invoice invoice = invoices.groupInvoice("G01");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void unknownRecipients() {
        assertThrows(NotFoundException.class, () -> invoices.create("USER", "nobody@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.create("GROUP", "G99"));
        assertThrows(ValidationException.class, () -> invoices.create("TICKET", "T001"));
    }
}
