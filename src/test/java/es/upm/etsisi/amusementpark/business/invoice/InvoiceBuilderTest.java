package es.upm.etsisi.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    private final InvoiceDirector director = new InvoiceDirector();
    private final User ana = new User("12345678Z", "ana@correo.com");
    private final LocalDate date = LocalDate.of(2027, 4, 15);

    @Test
    void userInvoiceWithDirectTickets() {
        Invoice invoice = director.construct(new UserInvoiceBuilder(ana, List.of(
                new IndividualTicket("T001", date, ana, new BigDecimal("40.00")),
                new IndividualTicket("T002", date, ana, new BigDecimal("45.50")))));
        assertEquals("USER", invoice.getTitle());
        assertEquals(List.of("User: ana@correo.com", "DNI: 12345678Z"), invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals(2, invoice.getSections().get(0).items().size());
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice user = director.construct(new UserInvoiceBuilder(ana, List.of()));
        assertTrue(user.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), user.getTotal());
        Invoice group = director.construct(new GroupInvoiceBuilder(new Group("G01", "Familia"), List.of()));
        assertTrue(group.getSections().isEmpty());
        assertEquals(List.of("Group: G01", "Name: Familia"), group.getRecipient());
    }

    @Test
    void groupInvoiceShowsCompositionAndGroupTicketTotals() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember(ana);
        group.addMember(new User("87654321X", "pedro@correo.com"));
        group.addMember(new User("11111111H", "luis@correo.com"));
        GroupTicket ticket = GroupTicket.purchase("TG01", date, group, new BigDecimal("40.00"));
        Invoice invoice = director.construct(new GroupInvoiceBuilder(group, List.of(ticket)));
        InvoiceItem item = invoice.getSections().get(0).items().get(0);
        assertEquals(new BigDecimal("108.00"), item.amount());
        assertEquals(3, item.details().size());
        assertTrue(item.details().get(0).startsWith("TG01:12345678Z ana@correo.com 36.00"));
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void builderCanBeReused() {
        UserInvoiceBuilder builder = new UserInvoiceBuilder(ana, List.of());
        director.construct(builder);
        assertEquals(2, director.construct(builder).getRecipient().size());
    }
}
