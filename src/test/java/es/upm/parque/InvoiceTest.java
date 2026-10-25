package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceTest {

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 1));
        t.run(ParkTestSupport.CREATE_ANA, ParkTestSupport.CREATE_PEDRO,
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "SPEEDPASS CREATE;T001;1",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP CREATE;G02;Vacio");
        t.addMembers("G01", 1);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SPECIALPASS CREATE;TG01-1");
    }

    @Test
    void userInvoiceHasDirectTicketsAndAllPasses() {
        Invoice invoice = InvoiceController.getInstance().userInvoice("ana@correo.com");
        assertEquals(2, invoice.sections().size());
        assertEquals("Individual tickets", invoice.sections().get(0).title());
        assertEquals(List.of(new InvoiceLine("T001 2026-10-15", new BigDecimal("40.00"))),
                invoice.sections().get(0).lines());
        assertEquals(2, invoice.sections().get(1).lines().size());
        assertEquals(new BigDecimal("88.00"), invoice.total());

        String out = t.output("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(out.startsWith("INVOICE USER ana@correo.com (Ana Garcia Lopez)"));
        assertTrue(out.contains("T001-SP1 SPEEDPASS (ticket T001) -> 8.00"));
        assertTrue(out.contains("TG01-1-SPECIAL SPECIAL PASS (ticket TG01-1) -> 40.00"));
        assertFalse(out.contains("TG01-1 2026-10-15"));
        assertTrue(out.trim().endsWith("TOTAL: 88.00"));
    }

    @Test
    void groupInvoiceHasOnlyGroupTickets() {
        String out = t.output("INVOICE CREATE;GROUP;G01");
        assertTrue(out.startsWith("INVOICE GROUP G01 (Familia)"));
        assertTrue(out.contains("Group tickets:"));
        assertTrue(out.contains("TG01 2026-10-15 x3 -> 108.00"));
        assertFalse(out.contains("SPECIAL"));
        assertFalse(out.contains("T001"));
        assertTrue(out.trim().endsWith("TOTAL: 108.00"));
    }

    @Test
    void recipientsWithoutPurchasesHaveNoSections() {
        String user = t.output("INVOICE CREATE;USER;pedro@correo.com");
        assertEquals(Stream.of("INVOICE USER pedro@correo.com (Pedro Lopez Martin)", "  TOTAL: 0.00", "")
                .reduce((a, b) -> a + System.lineSeparator() + b).orElseThrow(), user);
        assertTrue(t.output("INVOICE CREATE;GROUP;G02").contains("TOTAL: 0.00"));
    }

    @Test
    void unknownRecipientsAreReported() {
        assertTrue(t.run("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(t.run("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
    }

    @Test
    void invoicesAreNotPersisted() throws IOException {
        List<Path> before;
        try (Stream<Path> files = Files.list(dir)) {
            before = files.sorted().toList();
        }
        long modified = Files.getLastModifiedTime(dir.resolve("tickets.txt")).toMillis();
        t.run("INVOICE CREATE;USER;ana@correo.com", "INVOICE CREATE;GROUP;G01");
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(before, files.sorted().toList());
        }
        assertEquals(modified, Files.getLastModifiedTime(dir.resolve("tickets.txt")).toMillis());
    }

    @Test
    void builderRequiresRecipientFirst() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        assertThrows(IllegalStateException.class, builder::build);
        Invoice invoice = builder.start("X").addSection("Empty", List.of()).build();
        assertTrue(invoice.sections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.total());
    }
}
