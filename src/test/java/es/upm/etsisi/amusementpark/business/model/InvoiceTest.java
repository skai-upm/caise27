package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void totalsAddEveryLine() {
        InvoiceSection a = new InvoiceSection("A", List.of(new InvoiceLine("x", new BigDecimal("40.00")),
                new InvoiceLine("y", new BigDecimal("60.00"))));
        InvoiceSection b = new InvoiceSection("B", List.of(new InvoiceLine("z", new BigDecimal("108.00"))));
        Invoice invoice = new Invoice("R", LocalDate.of(2026, 10, 1), List.of(a, b));
        assertEquals(new BigDecimal("100.00"), a.getSubtotal());
        assertEquals("208.00", invoice.getTotal().toPlainString());
        assertEquals("R", invoice.getRecipient());
        assertEquals(LocalDate.of(2026, 10, 1), invoice.getIssueDate());
        assertThrows(UnsupportedOperationException.class, () -> invoice.getSections().add(a));
    }

    @Test
    void emptyInvoiceTotalsZero() {
        assertEquals("0.00", new Invoice("R", LocalDate.of(2026, 10, 1), List.of()).getTotal().toPlainString());
    }
}
