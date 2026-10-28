package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

class InvoiceBuilderTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void userInvoiceWithoutTicketsHasNoSections() {
        Invoice invoice = new UserInvoiceBuilder().user(ana).build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        assertTrue(invoice.getHeader().contains("DNI: 12345678Z"));
    }

    @Test
    void userInvoiceSumsDirectTickets() {
        Invoice invoice = new UserInvoiceBuilder().user(ana)
                .directTicket(new IndividualTicket("T001", day, ana, new BigDecimal("40.00")))
                .directTicket(new IndividualTicket("T002", day, ana, new BigDecimal("45.50"))).build();
        assertEquals(1, invoice.getSections().size());
        assertEquals(2, invoice.getSections().get(0).items().size());
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
    }

    @Test
    void groupInvoiceChargesTicketTotalAndDescribesComposition() {
        Group g = new Group("G01", "Familia");
        GroupTicket tg = new GroupTicket("TG01", day, g, List.of(ana, pedro), new BigDecimal("40.00"));
        Invoice invoice = new GroupInvoiceBuilder().group(g).groupTicket(tg).build();
        List<InvoiceItem> items = invoice.getSections().get(0).items();
        assertEquals(3, items.size());
        assertEquals(new BigDecimal("80.00"), items.get(0).getAmount().orElseThrow());
        assertTrue(items.get(1).getAmount().isEmpty());
        assertTrue(items.get(1).getDescription().contains("TG01:12345678Z"));
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
    }
}
