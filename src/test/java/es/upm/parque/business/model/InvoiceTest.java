package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void totalIsTheSumOfSectionSubtotals() {
        InvoiceSection individual = new InvoiceSection("Individual tickets", List.of(
            new InvoiceLine("T001", new BigDecimal("40.00")), new InvoiceLine("T002", new BigDecimal("60.00"))));
        InvoiceSection group = new InvoiceSection("Group tickets", List.of(new InvoiceLine("TG01", new BigDecimal("108.00"))));
        Invoice invoice = new Invoice("Ana", LocalDate.of(2026, 10, 15), List.of(individual, group));

        assertEquals(new BigDecimal("100.00"), individual.getSubtotal());
        assertEquals(new BigDecimal("208.00"), invoice.getTotal());
        assertEquals("Ana", invoice.getRecipient());
        assertEquals(LocalDate.of(2026, 10, 15), invoice.getIssueDate());
    }

    @Test
    void emptyInvoiceTotalsZero() {
        Invoice invoice = new Invoice("Pedro", LocalDate.of(2026, 10, 15), List.of());
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getTotal()));
        assertEquals(List.of(), invoice.getSections());
    }

    @Test
    void invoiceIsImmutable() {
        List<InvoiceLine> lines = new ArrayList<>(List.of(new InvoiceLine("T001", BigDecimal.TEN)));
        InvoiceSection section = new InvoiceSection("S", lines);
        lines.clear();
        assertEquals(1, section.getLines().size());
        Invoice invoice = new Invoice("X", LocalDate.of(2026, 1, 1), List.of(section));
        assertThrows(UnsupportedOperationException.class, () -> invoice.getSections().clear());
    }
}
