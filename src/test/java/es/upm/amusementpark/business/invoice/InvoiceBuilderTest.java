package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    @Test
    void userInvoiceTotalsItsTickets() {
        Invoice invoice = new UserInvoiceBuilder().forUser(ana)
                .addIndividualTicket(new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00")))
                .addIndividualTicket(new IndividualTicket("T002", DAY, ana, new BigDecimal("45.50")))
                .build();
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
        assertEquals(List.of("INVOICE - USER", "Name: Ana Garcia Lopez", "DNI: 12345678Z", "Email: ana@correo.com",
                "Individual tickets:", "  T001 (2027-04-15): 40.00 EUR", "  T002 (2027-04-15): 45.50 EUR",
                "TOTAL: 85.50 EUR"), invoice.render());
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice invoice = new UserInvoiceBuilder().forUser(pedro).build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        assertEquals("TOTAL: 0.00 EUR", invoice.render().get(4));
    }

    @Test
    void groupInvoiceShowsCompositionWithoutCountingItTwice() {
        Group group = new Group("G01", "Familia Garcia");
        GroupTicket ticket = new GroupTicket("TG01", DAY, group, List.of(
                new IndividualTicket("TG01:12345678Z", DAY, ana, new BigDecimal("40.00")),
                new IndividualTicket("TG01:87654321X", DAY, pedro, new BigDecimal("40.00"))));
        Invoice invoice = new GroupInvoiceBuilder().forGroup(group).addGroupTicket(ticket).build();
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        assertEquals(List.of("INVOICE - GROUP", "Group: G01 - Familia Garcia", "Group tickets:",
                "  TG01 (2027-04-15): 80.00 EUR", "    TG01:12345678Z - ana@correo.com (40.00 EUR)",
                "    TG01:87654321X - pedro@correo.com (40.00 EUR)", "TOTAL: 80.00 EUR"), invoice.render());
    }

    @Test
    void buildersShareACommonAbstraction() {
        assertEquals(InvoiceBuilder.class, UserInvoiceBuilder.class.getSuperclass());
        assertEquals(InvoiceBuilder.class, GroupInvoiceBuilder.class.getSuperclass());
    }
}
