package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class StandardInvoiceBuilderTest {

    @Test
    void buildsInStepsAndTotalsLines() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("USER", "ana@correo.com", "Ana")
                .section("Tickets", List.of(new InvoiceLine("a", new BigDecimal("40.00")),
                        new InvoiceLine("b", new BigDecimal("36.00"))))
                .section("Passes", List.of(new InvoiceLine("c", new BigDecimal("7.20"))))
                .build();

        assertEquals("USER", invoice.getRecipientType());
        assertEquals(2, invoice.getSections().size());
        assertEquals(new BigDecimal("83.20"), invoice.getTotal());
    }

    @Test
    void omitsEmptySections() {
        Invoice invoice = new StandardInvoiceBuilder()
                .recipient("GROUP", "G01", "Grupo")
                .section("Group tickets", List.of())
                .build();

        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void requiresRecipient() {
        StandardInvoiceBuilder builder = new StandardInvoiceBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
