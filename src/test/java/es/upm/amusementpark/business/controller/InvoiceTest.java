package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * User and group invoices built with the Builder pattern (Delivery 3, F3-C02).
 */
class InvoiceTest extends ParkTestSupport {

    private static List<String> descriptions(InvoiceSection section) {
        return section.lines().stream().map(InvoiceLine::description).toList();
    }

    @Test
    void buildersShareAbstraction() {
        assertTrue(InvoiceBuilder.class.isAssignableFrom(UserInvoiceBuilder.class));
        assertTrue(InvoiceBuilder.class.isAssignableFrom(GroupInvoiceBuilder.class));
    }

    @Test
    void userInvoiceExample() throws Exception {
        createAna();
        createPedro();
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpecialPass("PE01", "T001");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.buySpeedPasses("SP02", "TG01:12345678Z", "1");

        Invoice invoice = park.userInvoice("ana@correo.com");

        assertTrue(invoice.getHeader().contains("DNI: 12345678Z"));
        List<String> tickets = descriptions(invoice.getSection(UserInvoiceBuilder.TICKETS));
        assertEquals(1, tickets.size());
        assertTrue(tickets.get(0).contains("T001"));
        List<String> speed = descriptions(invoice.getSection(UserInvoiceBuilder.SPEED_PASSES));
        assertEquals(2, speed.size());
        assertTrue(speed.get(0).contains("SP01:1") && speed.get(1).contains("SP02:1"));
        assertTrue(descriptions(invoice.getSection(UserInvoiceBuilder.SPECIAL_PASSES)).get(0).contains("PE01"));
        assertEquals(new BigDecimal("96.00"), invoice.getTotal());
    }

    @Test
    void emptySectionsOmitted() throws Exception {
        createAna();
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        Invoice invoice = park.userInvoice("ana@correo.com");
        assertEquals(1, invoice.getSections().size());
        assertNull(invoice.getSection(UserInvoiceBuilder.SPEED_PASSES));
        assertNull(invoice.getSection(UserInvoiceBuilder.SPECIAL_PASSES));
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());

        createPedro();
        Invoice empty = park.userInvoice("pedro@correo.com");
        assertTrue(empty.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), empty.getTotal());
    }

    @Test
    void groupInvoiceExample() throws Exception {
        createGroupWith("G02", 1, 6);
        park.createGroupTicket("TG02", "G02", TODAY_TEXT);
        park.buySpeedPasses("SP01", "TG02:" + park.findUser("user1@correo.com").getDni(), "1");

        Invoice invoice = park.groupInvoice("G02");

        assertTrue(invoice.getHeader().contains("Group: G02"));
        assertTrue(invoice.getHeader().contains("Name: Group G02"));
        InvoiceSection section = invoice.getSection(GroupInvoiceBuilder.GROUP_TICKETS);
        assertEquals(1, section.lines().size());
        InvoiceLine line = section.lines().get(0);
        assertEquals(new BigDecimal("192.00"), line.amount());
        assertEquals(6, line.details().size());
        assertTrue(line.details().get(0).contains("32.00"));
        assertEquals(new BigDecimal("192.00"), invoice.getTotal());
        assertEquals(1, invoice.getSections().size());
    }

    @Test
    void errors() throws Exception {
        assertThrows(NotFoundException.class, () -> park.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> park.groupInvoice("G99"));
        createAna();
        park.deleteUser("ana@correo.com", "Clave1!");
        assertThrows(NotFoundException.class, () -> park.userInvoice("ana@correo.com"));
        park.createGroup("G01", "Vacio");
        assertNotNull(park.groupInvoice("G01"));
    }
}
