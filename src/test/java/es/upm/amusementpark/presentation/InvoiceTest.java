package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceTest extends ParkTestSupport {

    @BeforeEach
    void purchases() {
        run(ANA);
        run(PEDRO);
        createUser(3);
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;u3@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("GROUPTICKET CREATE;TG02;G01;2026-10-16");
    }

    @Test
    void userInvoiceListsDirectTicketsOnly() {
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.startsWith("INVOICE USER ana@correo.com - Ana Garcia Lopez"));
        assertTrue(invoice.contains("Individual tickets:"));
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) 40.00"));
        assertFalse(invoice.contains("TG01"));
        assertTrue(invoice.endsWith("TOTAL: 40.00"));
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        String invoice = run("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("Individual tickets:"));
        assertTrue(invoice.endsWith("TOTAL: 0.00"));
    }

    @Test
    void groupInvoiceListsGroupTicketsAndTotal() {
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE GROUP G01 - Familia"));
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members) 108.00"));
        assertTrue(invoice.contains("Group ticket TG02 (2026-10-16, 3 members) 108.00"));
        assertFalse(invoice.contains("T001"));
        assertFalse(invoice.contains("TG01-1"));
        assertTrue(invoice.endsWith("TOTAL: 216.00"));
    }

    @Test
    void rejectsUnknownRecipients() {
        assertTrue(error(run("INVOICE CREATE;USER;nadie@correo.com")));
        assertTrue(error(run("INVOICE CREATE;GROUP;G99")));
        assertTrue(error(run("INVOICE CREATE;OTHER;x")));
        assertTrue(error(run("INVOICE CREATE;USER")));
    }

    @Test
    void invoicesAreNotPersisted() throws IOException {
        long before;
        try (Stream<?> files = Files.list(dir)) {
            before = files.count();
        }
        run("INVOICE CREATE;GROUP;G01");
        try (Stream<?> files = Files.list(dir)) {
            assertEquals(before, files.count());
        }
    }
}
