package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class TextInvoiceBuilderTest {

    @Test
    void buildsStepByStepAndOmitsEmptySections() {
        Invoice invoice = new TextInvoiceBuilder()
                .forRecipient("USER", "Ana")
                .addSection("Individual tickets", List.of(new InvoiceLine("T001", new BigDecimal("40.00")),
                        new InvoiceLine("T002", new BigDecimal("60.00"))))
                .addSection("Personal passes", List.of())
                .build();
        assertEquals(1, invoice.getSections().size());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
        String text = invoice.render();
        assertTrue(text.contains("INVOICE - USER: Ana"));
        assertTrue(text.contains("T001 -> 40.00"));
        assertTrue(text.contains("TOTAL: 100.00"));
        assertFalse(text.contains("Personal passes"));
    }

    @Test
    void newRecipientResetsSections() {
        TextInvoiceBuilder builder = new TextInvoiceBuilder();
        builder.forRecipient("USER", "Ana").addSection("S", List.of(new InvoiceLine("x", BigDecimal.ONE)));
        Invoice empty = builder.forRecipient("GROUP", "G01").build();
        assertTrue(empty.getSections().isEmpty());
        assertEquals(0, empty.getTotal().signum());
    }

    @Test
    void recipientIsRequired() {
        TextInvoiceBuilder builder = new TextInvoiceBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
