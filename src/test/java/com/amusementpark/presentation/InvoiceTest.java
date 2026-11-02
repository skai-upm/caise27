package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.ok("GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com");
        f.user("Pedro", 87654321);
        f.user("Luis", 11111111);
        f.ok("GROUP ADD;G01;pedro@correo.com", "GROUP ADD;G01;luis@correo.com");
    }

    @Test
    void userInvoiceListsStandaloneTicketsOnly() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15", "SYSTEM PRICE;60.00",
                "TICKET CREATE;T002;ana@correo.com;2026-10-16", "GROUPTICKET CREATE;TG01;G01;2026-10-20");
        String invoice = f.run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.startsWith("INVOICE - User Ana Apellido (ana@correo.com)"));
        assertTrue(invoice.contains("Ticket T001 (2026-10-15)  40.00"));
        assertTrue(invoice.contains("Ticket T002 (2026-10-16)  60.00"));
        assertFalse(invoice.contains("TG01"), "constituent tickets are not counted again");
        assertTrue(invoice.endsWith("TOTAL: 100.00"));
    }

    @Test
    void groupInvoiceListsGroupTicketsWithoutConstituents() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "GROUPTICKET CREATE;TG02;G01;2026-10-16");
        String invoice = f.run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE - Group G01 Familia"));
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members)  108.00"));
        assertTrue(invoice.contains("Group ticket TG02 (2026-10-16, 3 members)  108.00"));
        assertFalse(invoice.contains("TG01-1"));
        assertFalse(invoice.contains("T001"));
        assertTrue(invoice.endsWith("TOTAL: 216.00"));
    }

    @Test
    void omitsEmptySectionsAndShowsZeroTotal() {
        String invoice = f.run("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("Tickets:"));
        assertFalse(invoice.contains("Group tickets:"));
        assertTrue(invoice.endsWith("TOTAL: 0.00"));
        f.ok("TICKET CREATE;T001;pedro@correo.com;2026-10-15");
        assertFalse(f.run("INVOICE CREATE;USER;pedro@correo.com").contains("Group tickets:"));
        assertFalse(f.run("INVOICE CREATE;GROUP;G01").contains("Tickets:"));
    }

    @Test
    void rejectsUnknownRecipients() {
        assertTrue(f.run("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("INVOICE CREATE;GROUP;G99").startsWith("ERROR:"));
        assertTrue(f.run("INVOICE CREATE;COMPANY;G01").startsWith("ERROR:"));
        assertTrue(f.run("INVOICE CREATE;USER").startsWith("ERROR:"));
    }

    @Test
    void invoicesAreNotPersisted() throws IOException {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        long before;
        try (Stream<Path> s = Files.list(dir)) {
            before = s.count();
        }
        f.ok("INVOICE CREATE;USER;ana@correo.com", "INVOICE CREATE;GROUP;G01");
        try (Stream<Path> s = Files.list(dir)) {
            assertEquals(before, s.count());
        }
    }
}
