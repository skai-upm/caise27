package com.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parque.business.model.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceControllerTest {
    @TempDir
    Path dir;
    ParkController c;

    @BeforeEach
    void setUp() throws Exception {
        ParkController opened = ParkController.open(dir);
        c = opened;
        c.setSystemDate(LocalDate.of(2026, 10, 1));
        c.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        c.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        c.createUser("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        c.createGroup("G01", "Familia Garcia");
        c.addGroupMember("G01", "ana@correo.com");
        c.addGroupMember("G01", "pedro@correo.com");
        c.addGroupMember("G01", "luis@correo.com");
    }

    @Test
    void userInvoiceDoesNotChargeConstituents() {
        c.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        c.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 15));
        Invoice invoice = c.createUserInvoice("ana@correo.com");
        List<InvoiceLine> lines = invoice.getSections().get(0).getLines();
        assertEquals(1, lines.size());
        assertEquals("T001 (2026-10-15)", lines.get(0).getDescription());
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());
        Invoice pedro = c.createUserInvoice("pedro@correo.com");
        assertEquals(0, pedro.getSections().size(), "constituent-only user has no tickets section");
    }

    @Test
    void groupInvoiceChargesOnlyTheGroupTicket() {
        c.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        c.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 15));
        Invoice invoice = c.createGroupInvoice("G01");
        assertEquals(1, invoice.getSections().size());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void pricesAsPurchasedAndOnDemand() throws Exception {
        c.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        assertEquals(new BigDecimal("40.00"), c.createUserInvoice("ana@correo.com").getTotal());
        c.setTicketPrice(new BigDecimal("60.00"));
        c.createTicket("T002", "ana@correo.com", LocalDate.of(2026, 10, 20));
        assertEquals(new BigDecimal("100.00"), c.createUserInvoice("ana@correo.com").getTotal());
        try (var files = Files.list(dir)) {
            assertEquals(List.of(), files.map(p -> p.getFileName().toString()).filter(n -> n.contains("invoice")).toList());
        }
    }

    @Test
    void unknownRecipients() {
        assertThrows(IllegalArgumentException.class, () -> c.createUserInvoice("nadie@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> c.createGroupInvoice("G99"));
    }
}
