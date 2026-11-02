package es.upm.etsisi.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.etsisi.amusementpark.business.user.User;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    @Test
    void emptySectionsAreOmittedAndDetailsNotCounted() {
        User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "h", "6", "1");
        Invoice invoice = new UserInvoiceBuilder().forUser(ana).withTickets(List.of()).withSpeedPasses(List.of())
                .withSpecialPasses(List.of()).build();
        assertEquals(List.of(), invoice.sections());
        assertEquals(3, invoice.header().size());
        assertEquals(BigDecimal.ZERO, invoice.total());
        InvoiceSection section = new InvoiceSection("S", List.of(InvoiceLine.charge("a", new BigDecimal("10.00")),
                InvoiceLine.detail("b", new BigDecimal("5.00"))));
        assertEquals(new BigDecimal("10.00"), section.total());
    }

    @Test
    void buildRequiresReset() {
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
