package es.upm.parque.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");

    @Test
    void builderAssemblesHeaderSectionsAndTotal() {
        InvoiceBuilder builder = new DefaultInvoiceBuilder();
        builder.buildHeader("USER", "12345678Z", "Ana Garcia Lopez", TODAY);
        builder.buildSection("Individual tickets", List.of(new InvoiceLine("T001", new BigDecimal("40.00")),
                new InvoiceLine("T002", new BigDecimal("60.00"))));
        builder.buildSection("Other", List.of(new InvoiceLine("X", new BigDecimal("8.00"))));
        Invoice invoice = builder.getResult();
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertEquals(2, invoice.getSections().size());
        assertEquals(new BigDecimal("100.00"), invoice.getSections().get(0).getSubtotal());
        String text = invoice.format();
        assertTrue(text.startsWith("INVOICE USER 12345678Z - Ana Garcia Lopez"), text);
        assertTrue(text.contains("Date: 2026-10-01") && text.contains("T002") && text.endsWith("TOTAL: 108.00"), text);
    }

    @Test
    void resetDiscardsPreviousState() {
        InvoiceBuilder builder = new DefaultInvoiceBuilder();
        builder.buildHeader("USER", "1", "A", TODAY);
        builder.buildSection("S", List.of(new InvoiceLine("X", BigDecimal.TEN)));
        builder.reset();
        assertThrows(IllegalStateException.class, builder::getResult);
        builder.buildHeader("GROUP", "G01", "Amigos", TODAY);
        assertEquals(0, builder.getResult().getSections().size());
    }

    @Test
    void directorIncludesOnlySectionsWithData() {
        InvoiceDirector director = new InvoiceDirector();
        InvoiceBuilder builder = new DefaultInvoiceBuilder();
        Invoice none = director.constructUserInvoice(builder, ana, List.of(), List.of(), TODAY);
        assertEquals(0, none.getSections().size());
        assertEquals("0.00", none.getTotal().toPlainString());

        IndividualTicket t = new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
        Invoice user = director.constructUserInvoice(builder, ana, List.of(t), List.of(t), TODAY);
        assertEquals("Individual tickets", user.getSections().get(0).getTitle());
        assertEquals("40.00", user.getTotal().toPlainString());

        Group group = new Group("G01", "Amigos");
        group.addMember(ana);
        GroupTicket gt = GroupTicket.purchase("TG01", DAY, group,
                List.of(new IndividualTicket("TG01-1", DAY, ana, new BigDecimal("40.00"))));
        Invoice groupInvoice = director.constructGroupInvoice(builder, group, List.of(gt), TODAY);
        assertEquals("GROUP", groupInvoice.getRecipientType());
        assertEquals(1, groupInvoice.getSections().get(0).getLines().size());
        assertEquals("40.00", groupInvoice.getTotal().toPlainString());
    }
}
