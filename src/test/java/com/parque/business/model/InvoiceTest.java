package com.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void totalsAndReadOnlyParts() {
        InvoiceSection s1 = new InvoiceSection("Individual tickets",
                List.of(new InvoiceLine("T001", new BigDecimal("40")), new InvoiceLine("T002", new BigDecimal("60.5"))));
        assertEquals(new BigDecimal("100.50"), s1.getSubtotal());
        Invoice invoice = new Invoice("user Ana", List.of(s1));
        assertEquals(new BigDecimal("100.50"), invoice.getTotal());
        assertThrows(UnsupportedOperationException.class, () -> invoice.getSections().clear());
        assertThrows(UnsupportedOperationException.class, () -> s1.getLines().clear());
        assertEquals(new BigDecimal("0.00"), new Invoice("user Pedro", List.of()).getTotal());
    }
}
