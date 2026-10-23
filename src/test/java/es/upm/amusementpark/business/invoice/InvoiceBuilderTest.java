package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "6",
            "9876543210987654");
    private final User laura = new User("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "6",
            "1111222233334444");

    @Test
    void buildersShareTheBuilderAbstraction() {
        assertTrue(InvoiceBuilder.class.isAssignableFrom(UserInvoiceBuilder.class));
        assertTrue(InvoiceBuilder.class.isAssignableFrom(GroupInvoiceBuilder.class));
        assertTrue(AbstractInvoiceBuilder.class.isAssignableFrom(UserInvoiceBuilder.class));
        assertEquals(1, Invoice.class.getDeclaredConstructors().length);
        assertTrue(!java.lang.reflect.Modifier.isPublic(Invoice.class.getDeclaredConstructors()[0].getModifiers()),
                "invoices are only obtained from a builder");
    }

    @Test
    void userInvoiceWithTickets() {
        Invoice invoice = new UserInvoiceBuilder().issuedOn(DAY).customer(ana)
                .individualTickets(List.of(new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00")),
                        new IndividualTicket("T002", DAY, ana, new BigDecimal("45.50"))))
                .build();
        assertEquals(DAY, invoice.getDate());
        assertTrue(invoice.getHeader().contains("DNI: 12345678Z"));
        assertEquals(1, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).title());
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice invoice = new UserInvoiceBuilder().issuedOn(DAY).customer(ana).individualTickets(List.of())
                .section("Extras", List.of()).build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceCountsEachGroupTicketOnce() {
        Group group = new Group("G01", "Familia Garcia");
        group.add(ana);
        group.add(pedro);
        group.add(laura);
        GroupTicket ticket = GroupTicket.purchase("TG01", DAY, group, new BigDecimal("40.00"));
        Invoice invoice = new GroupInvoiceBuilder().issuedOn(DAY).group(group).groupTickets(List.of(ticket)).build();
        assertEquals(List.of("Group: G01 - Familia Garcia"), invoice.getHeader());
        InvoiceItem item = invoice.getSections().get(0).items().get(0);
        assertEquals(new BigDecimal("108.00"), item.amount());
        assertEquals(3, item.details().size());
        assertTrue(item.details().get(0).startsWith("TG01:12345678Z Ana Garcia Lopez (36.00 EUR)"));
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void buildRequiresDateAndRecipient() {
        UserInvoiceBuilder withoutRecipient = new UserInvoiceBuilder().issuedOn(DAY);
        assertThrows(IllegalStateException.class, withoutRecipient::build);
        GroupInvoiceBuilder withoutDate = new GroupInvoiceBuilder().group(new Group("G", "N"));
        assertThrows(IllegalStateException.class, withoutDate::build);
    }
}
