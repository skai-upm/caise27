package es.upm.parque.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class StandardInvoiceBuilderTest {

    @Test
    void assemblesSectionsStepByStepAndSumsTotal() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("Ana")
                .section("Individual tickets")
                .item("T001", new BigDecimal("40.00"))
                .item("T002", new BigDecimal("60.00"))
                .section("Passes")
                .item("SP1", new BigDecimal("8.00"))
                .build();
        assertEquals("Ana", invoice.getRecipient());
        assertEquals(2, invoice.getSections().size());
        assertEquals(List.of(new InvoiceLine("T001", new BigDecimal("40.00")), new InvoiceLine("T002", new BigDecimal("60.00"))),
                invoice.getSections().get(0).lines());
        assertEquals("Passes", invoice.getSections().get(1).title());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void emptySectionsAreOmittedAndBuilderResets() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        Invoice empty = builder.recipient("Pedro").section("Individual tickets").build();
        assertTrue(empty.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), empty.getTotal());

        Invoice next = builder.section("Group tickets").item("TG01", new BigDecimal("108.00")).build();
        assertNull(next.getRecipient());
        assertEquals(1, next.getSections().size());
        assertEquals(new BigDecimal("108.00"), next.getTotal());
    }

    @Test
    void itemRequiresSection() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        BigDecimal amount = BigDecimal.ONE;
        assertThrows(IllegalStateException.class, () -> builder.item("x", amount));
    }
}
