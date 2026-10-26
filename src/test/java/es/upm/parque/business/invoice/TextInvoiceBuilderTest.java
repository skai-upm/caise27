package es.upm.parque.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class TextInvoiceBuilderTest {

    private final TextInvoiceBuilder builder = new TextInvoiceBuilder();

    @Test
    void assemblesSectionsStepByStep() {
        builder.startInvoice("USER", "Ana");
        builder.startSection("Individual tickets");
        builder.addLine("T001", new BigDecimal("40.00"));
        builder.addLine("T002", new BigDecimal("60.00"));
        builder.startSection("Empty");
        builder.startSection("Other");
        builder.addLine("X", new BigDecimal("8.5"));
        Invoice invoice = builder.build();

        assertEquals(2, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).title());
        assertEquals(new BigDecimal("108.50"), invoice.getTotal());
        assertEquals(List.of("Invoice: USER", "Recipient: Ana", "Individual tickets:", "  - T001: 40.00",
                "  - T002: 60.00", "Other:", "  - X: 8.5", "TOTAL: 108.50"), invoice.toLines());
    }

    @Test
    void emptyInvoiceHasNoSections() {
        builder.startInvoice("USER", "Luis");
        builder.startSection("Individual tickets");
        Invoice invoice = builder.build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(List.of("Invoice: USER", "Recipient: Luis", "No billable items", "TOTAL: 0.00"), invoice.toLines());
    }

    @Test
    void startInvoiceResetsState() {
        builder.startInvoice("A", "a");
        builder.startSection("S");
        builder.addLine("x", BigDecimal.TEN);
        builder.build();
        builder.startInvoice("B", "b");
        Invoice second = builder.build();
        assertTrue(second.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), second.getTotal());
    }

    @Test
    void stepsMustBeInOrder() {
        assertThrows(IllegalStateException.class, builder::build);
        builder.startInvoice("A", "a");
        assertThrows(IllegalStateException.class, () -> builder.addLine("x", BigDecimal.ONE));
    }
}
