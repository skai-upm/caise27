package com.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.TestSupport;
import com.amusementpark.business.invoice.Invoice;
import com.amusementpark.presentation.Bootstrap;
import com.amusementpark.presentation.CommandProcessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class InvoiceScenarioTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws Exception {
        processor = Bootstrap.initialize(dir, LocalDate.of(2026, 10, 1), TestSupport.FAST_HASHER,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        run("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;luis@correo.com");
    }

    private String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    public void controllerIsSingleton() {
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    public void userInvoiceExampleFromSpecification() {
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("SPEEDPASS CREATE;T001;1");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SPECIALPASS CREATE;TG01-1");
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.startsWith("INVOICE USER ana@correo.com"), invoice);
        assertTrue(invoice.contains("Ticket T001 (2026-10-15): 40.00"), invoice);
        assertTrue(invoice.contains("T001-SP1: 8.00"), invoice);
        assertTrue(invoice.contains("TG01-1-SPECIAL: 40.00"), invoice);
        assertFalse(invoice.contains("Ticket TG01-1"), invoice);
        assertFalse(invoice.contains("Group ticket"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 88.00"), invoice);

        Invoice built = InvoiceController.getInstance()
                .userInvoice(UserController.getInstance().findByEmail("ana@correo.com"));
        assertEquals(3, built.getSections().size());
        assertEquals(new BigDecimal("88.00"), built.getTotal());
    }

    @Test
    public void groupInvoiceListsOnlyGroupTickets() {
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        run("SPEEDPASS CREATE;TG01-1;10");
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE GROUP G01 - Familia"), invoice);
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members): 108.00"), invoice);
        assertFalse(invoice.contains("SP1"), invoice);
        assertFalse(invoice.contains("Individual tickets"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 108.00"), invoice);
    }

    @Test
    public void emptyAndUnknownRecipients() {
        String invoice = run("INVOICE CREATE;USER;luis@correo.com");
        assertTrue(invoice.contains("No purchases") && invoice.endsWith("TOTAL: 0.00"), invoice);
        assertTrue(run("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(run("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(run("INVOICE CREATE;FAMILY;G01").startsWith("ERROR"));
        assertTrue(run("INVOICE CREATE;USER").startsWith("ERROR"));
    }
}
