package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SYSTEM PRICE;60.00", "TICKET CREATE;T002;ana@correo.com;2026-10-16", "SYSTEM PRICE;40.00");
        s.run("GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com");
        s.run("GROUP ADD;G01;" + s.createUser(1), "GROUP ADD;G01;" + s.createUser(2));
    }

    @Test
    void userInvoiceListsDirectTicketsOnly() {
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        String invoice = s.run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.startsWith("INVOICE - USER: Ana Garcia Lopez (ana@correo.com)"), invoice);
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) -> 40.00"));
        assertTrue(invoice.contains("Ticket T002 (2026-10-16) -> 60.00"));
        assertFalse(invoice.contains("TG01"));
        assertTrue(invoice.endsWith("TOTAL: 100.00"));
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        String invoice = s.run("INVOICE CREATE;USER;user1@correo.com");
        assertFalse(invoice.contains("Individual tickets"));
        assertTrue(invoice.endsWith("TOTAL: 0.00"));
    }

    @Test
    void groupInvoiceListsGroupTickets() {
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "GROUP CREATE;G02;Pareja",
                "GROUP ADD;G02;" + s.createUser(3), "GROUP ADD;G02;" + s.createUser(4),
                "GROUPTICKET CREATE;TG02;G02;2026-10-15");
        String invoice = s.run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE - GROUP: Familia Garcia (G01)"));
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members) -> 108.00"));
        assertFalse(invoice.contains("TG01-1"));
        assertFalse(invoice.contains("TG02"));
        assertFalse(invoice.contains("T001"));
        assertTrue(invoice.endsWith("TOTAL: 108.00"));
    }

    @Test
    void groupTotalSumsAllGroupTickets() {
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-16", "GROUP REMOVE;G01;user2@correo.com",
                "GROUPTICKET CREATE;TG02;G01;2026-10-20");
        assertTrue(s.run("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 188.00"));
    }

    @Test
    void invalidInvoiceRequests() {
        assertTrue(s.run("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(s.run("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(s.run("INVOICE CREATE;OTHER;G01").startsWith("ERROR"));
        assertTrue(s.run("INVOICE CREATE;USER").startsWith("ERROR"));
    }
}
