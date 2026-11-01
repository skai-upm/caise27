package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class StandardInvoiceBuilderTest {

    @Test
    void buildsInStepsAndTotalsSections() {
        Invoice invoice = new StandardInvoiceBuilder()
                .forRecipient("User Ana")
                .addSection("Tickets", List.of(new InvoiceLine("T001", new BigDecimal("40.00")),
                        new InvoiceLine("T002", new BigDecimal("60.00"))))
                .addSection("Passes", List.of(new InvoiceLine("T001-SP1", new BigDecimal("8.00"))))
                .build();
        assertEquals("User Ana", invoice.getRecipient());
        assertEquals(2, invoice.getSections().size());
        assertEquals(new BigDecimal("100.00"), invoice.getSections().get(0).subtotal());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void omitsEmptySections() {
        Invoice invoice = new StandardInvoiceBuilder()
                .forRecipient("User Ana")
                .addSection("Tickets", List.of())
                .build();
        assertEquals(List.of(), invoice.getSections());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void requiresRecipient() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
