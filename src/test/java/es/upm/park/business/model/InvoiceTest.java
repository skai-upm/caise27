package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void totalsAddUp() {
        InvoiceSection a = new InvoiceSection("A", List.of(new InvoiceLine("x", new BigDecimal("40.00")),
                new InvoiceLine("y", new BigDecimal("60.00"))));
        InvoiceSection b = new InvoiceSection("B", List.of(new InvoiceLine("z", new BigDecimal("108.00"))));
        assertEquals(new BigDecimal("100.00"), a.subtotal());
        assertEquals(new BigDecimal("208.00"), new Invoice("r", List.of(a, b)).total());
        assertEquals(new BigDecimal("0.00"), new Invoice("r", List.of()).total());
    }

    @Test
    void isImmutable() {
        List<InvoiceLine> lines = new ArrayList<>(List.of(new InvoiceLine("x", BigDecimal.ONE)));
        InvoiceSection s = new InvoiceSection("A", lines);
        lines.clear();
        assertEquals(1, s.lines().size());
        assertThrows(UnsupportedOperationException.class, () -> s.lines().add(new InvoiceLine("y", BigDecimal.ONE)));
    }
}
