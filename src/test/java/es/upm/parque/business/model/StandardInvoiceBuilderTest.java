package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class StandardInvoiceBuilderTest {

    private static final String NL = System.lineSeparator();

    @Test
    void assemblesInvoiceInSteps() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("USER", "ana@correo.com", "Ana Garcia Lopez")
                .issuedOn(LocalDate.of(2026, 10, 1))
                .section("Individual tickets")
                .line("T001 2026-10-15", new BigDecimal("40.00"))
                .line("T002 2026-10-20", new BigDecimal("60.00"))
                .build();
        assertEquals("USER", invoice.getRecipientKind());
        assertEquals("ana@correo.com", invoice.getRecipientId());
        assertEquals("Ana Garcia Lopez", invoice.getRecipientName());
        assertEquals(LocalDate.of(2026, 10, 1), invoice.getIssueDate());
        assertEquals(1, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).getTitle());
        assertEquals(2, invoice.getSections().get(0).getLines().size());
        assertEquals("T002 2026-10-20", invoice.getSections().get(0).getLines().get(1).getDescription());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
        assertEquals(String.join(NL, "INVOICE USER ana@correo.com - Ana Garcia Lopez", "Date: 2026-10-01",
                "Individual tickets:", "- T001 2026-10-15 40.00", "- T002 2026-10-20 60.00", "TOTAL: 100.00"),
                invoice.toText());
    }

    @Test
    void omitsEmptySections() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("GROUP", "G01", "Familia")
                .issuedOn(LocalDate.of(2026, 10, 1))
                .section("Group tickets")
                .section("Passes")
                .line("P1", new BigDecimal("8.00"))
                .section("Empty")
                .build();
        assertEquals(1, invoice.getSections().size());
        assertEquals("Passes", invoice.getSections().get(0).getTitle());
    }

    @Test
    void invoiceWithoutPurchases() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("USER", "ana@correo.com", "Ana Garcia Lopez")
                .issuedOn(LocalDate.of(2026, 10, 1))
                .section("Individual tickets")
                .build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(String.join(NL, "INVOICE USER ana@correo.com - Ana Garcia Lopez", "Date: 2026-10-01",
                "No purchases", "TOTAL: 0.00"), invoice.toText());
    }

    @Test
    void rejectsLineWithoutSection() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        BigDecimal amount = BigDecimal.ONE;
        assertThrows(IllegalStateException.class, () -> builder.line("x", amount));
    }

    @Test
    void sectionsOfBuiltInvoiceCannotBeModified() {
        Invoice invoice = new StandardInvoiceBuilder().recipient("USER", "a", "A").issuedOn(LocalDate.of(2026, 1, 1))
                .section("S").line("x", BigDecimal.ONE).build();
        List<InvoiceSection> sections = invoice.getSections();
        InvoiceSection section = sections.get(0);
        List<InvoiceLine> lines = section.getLines();
        assertThrows(UnsupportedOperationException.class, () -> sections.add(section));
        assertThrows(UnsupportedOperationException.class, () -> lines.add(null));
    }
}
