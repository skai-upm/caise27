package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    @Test
    void emptySectionsAreOmittedAndTotalIsZero() {
        Invoice invoice = new UserInvoiceBuilder().forUser("ana@correo.com", "12345678Z").build();
        assertTrue(invoice.sections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.total());
        assertEquals(List.of("Email", "DNI"), List.copyOf(invoice.recipient().keySet()));
        assertEquals("USER", invoice.type());
    }

    @Test
    void onlyItemsAreCounted() {
        InvoiceBuilder builder = new InvoiceBuilder("TEST") {
            {
                item("S", "a", new BigDecimal("10.00"));
                detail("S", "info");
                item("T", "b", new BigDecimal("2.50"));
            }
        };
        Invoice invoice = builder.build();
        assertEquals(new BigDecimal("12.50"), invoice.total());
        assertEquals(List.of("S", "T"), invoice.sections().stream().map(Invoice.Section::title).toList());
        assertFalse(invoice.sections().get(0).lines().get(1).isItem());
    }

    @Test
    void passesAppearInTheirOwnSection() {
        Invoice invoice = new UserInvoiceBuilder().forUser("ana@correo.com", null)
                .addPass("SpeedPass SP1", new BigDecimal("5.00")).build();
        assertEquals(UserInvoiceBuilder.PASSES, invoice.sections().get(0).title());
        assertEquals(new BigDecimal("5.00"), invoice.total());
        assertFalse(invoice.recipient().containsKey("DNI"));
    }
}
