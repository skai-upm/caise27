package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    @Test
    void buildsStepByStepAndSumsItemsOnly() {
        Invoice invoice = new StandardInvoiceBuilder().forRecipient(Invoice.Type.USER)
                .recipient("Name", "Ana")
                .section("Tickets")
                .item("T001", new BigDecimal("40.00"))
                .item("T002", new BigDecimal("36.00"))
                .detail("not charged")
                .build();
        assertEquals(Invoice.Type.USER, invoice.getType());
        assertEquals("Ana", invoice.getRecipient().get(0).getValue());
        assertEquals(1, invoice.getSections().size());
        assertEquals(2, invoice.getSections().get(0).items().size());
        assertEquals(List.of("not charged"), invoice.getSections().get(0).items().get(1).details());
        assertEquals(new BigDecimal("76.00"), invoice.getTotal());
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice invoice = new StandardInvoiceBuilder().forRecipient(Invoice.Type.GROUP)
                .recipient("Group", "G01")
                .section("Group tickets")
                .build();
        assertEquals(0, invoice.getSections().size());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void stepsMustBeInOrder() {
        InvoiceBuilder b = new StandardInvoiceBuilder().forRecipient(Invoice.Type.USER);
        assertThrows(IllegalStateException.class, () -> b.item("x", BigDecimal.ONE));
        assertThrows(IllegalStateException.class, () -> new StandardInvoiceBuilder().build());
    }

    @Test
    void builderIsReusable() {
        StandardInvoiceBuilder b = new StandardInvoiceBuilder();
        b.forRecipient(Invoice.Type.USER).section("S").item("a", BigDecimal.TEN).build();
        Invoice second = b.forRecipient(Invoice.Type.GROUP).build();
        assertEquals(0, second.getSections().size());
        assertEquals(0, second.getRecipient().size());
    }
}
