package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.Invoice;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.SimpleInvoiceBuilder;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.AppBootstrap;

public class InvoiceTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    public void setUp() throws IOException, OperationException {
        park = AppBootstrap.start(new StoragePaths(dir), LocalDate.of(2026, 10, 1));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "600000000",
                "1234567890123456");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "luis@correo.com");
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    @Test
    public void userInvoiceListsOnlyDirectTickets() throws OperationException {
        park.createTicket("T001", "ana@correo.com", "2026-10-15");
        park.setBasePrice("60.00");
        park.createTicket("T002", "ana@correo.com", "2026-10-16");
        park.createTicket("T003", "pedro@correo.com", "2026-10-16");
        park.createGroupTicket("TG01", "G01", "2026-10-20");

        Invoice invoice = park.invoice("USER", "ana@correo.com");
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
        assertEquals(1, invoice.getSections().size());
        assertEquals(2, invoice.getSections().get(0).lines().size());
        String text = invoice.render();
        assertTrue(text.contains("INVOICE USER ana@correo.com - Ana Garcia Lopez"));
        assertTrue(text.contains("T001 (2026-10-15): 40.00"));
        assertFalse(text.contains("TG01"));
        assertTrue(text.endsWith("TOTAL: 100.00"));
    }

    @Test
    public void groupInvoiceListsGroupTicketsOnly() throws OperationException {
        park.createTicket("T001", "ana@correo.com", "2026-10-15");
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        park.setSystemDate("2026-10-16");
        park.removeFromGroup("G01", "luis@correo.com");
        park.createGroupTicket("TG02", "G01", "2026-10-17");

        Invoice invoice = park.invoice("GROUP", "G01");
        assertEquals(new BigDecimal("188.00"), invoice.getTotal());
        String text = invoice.render();
        assertTrue(text.contains("TG01 (2026-10-15, 3 members): 108.00"));
        assertTrue(text.contains("TG02 (2026-10-17, 2 members): 80.00"));
        assertFalse(text.contains("T001"));
        assertFalse(text.contains("TG01-1"));
    }

    @Test
    public void emptySectionsAreOmitted() throws OperationException {
        Invoice invoice = park.invoice("USER", "luis@correo.com");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(String.join(System.lineSeparator(), "INVOICE USER luis@correo.com - Luis Ruiz", "TOTAL: 0.00"),
                invoice.render());
    }

    @Test
    public void unknownRecipientsAreRejected() {
        assertThrows(OperationException.class, () -> park.invoice("USER", "nadie@correo.com"));
        assertThrows(OperationException.class, () -> park.invoice("GROUP", "G99"));
        assertThrows(OperationException.class, () -> park.invoice("CLIENT", "x"));
    }

    @Test
    public void builderAssemblesInSteps() {
        Invoice invoice = new SimpleInvoiceBuilder()
                .recipient("USER", "a@b.com", "A B")
                .section("Empty")
                .section("Things").item("x", new BigDecimal("1.50")).item("y", new BigDecimal("2.25"))
                .build();
        assertEquals(1, invoice.getSections().size());
        assertEquals("Things", invoice.getSections().get(0).title());
        assertEquals(new BigDecimal("3.75"), invoice.getTotal());
    }
}
