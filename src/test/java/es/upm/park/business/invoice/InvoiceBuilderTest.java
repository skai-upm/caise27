package es.upm.park.business.invoice;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceBuilderTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @Test
    void emptySectionsAreOmittedAndDetailsDoNotCount() {
        Invoice invoice = new UserInvoiceBuilder()
                .forUser("ana@correo.com", "12345678Z")
                .startSection("Empty")
                .startSection("Items")
                .item("A", new BigDecimal("10.00"), List.of("detail"))
                .build();
        assertEquals(1, invoice.getSections().size());
        assertEquals("Items", invoice.getSections().get(0).title());
        assertEquals(new BigDecimal("10.00"), invoice.total());
    }

    @Test
    void userInvoiceExcludesComposedTickets() {
        Map<String, String> members = new LinkedHashMap<>();
        members.put("ana@correo.com", "12345678Z");
        GroupTicket group = GroupTicket.purchase("TG01", new Group("G01", "g"), DAY, new BigDecimal("40.00"), members);
        List<IndividualTicket> tickets = List.of(
                new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00")),
                group.getComposition().get(0));
        Invoice invoice = new UserInvoiceBuilder().forUser("ana@correo.com", "12345678Z").directTickets(tickets).build();
        String text = invoice.render();
        assertTrue(text.contains("T001"));
        assertFalse(text.contains("TG01:12345678Z"));
        assertEquals(new BigDecimal("40.00"), invoice.total());
    }

    @Test
    void userWithoutPurchasesHasNoTicketSection() {
        Invoice invoice = new UserInvoiceBuilder().forUser("pedro@correo.com", "87654321X")
                .directTickets(List.of()).build();
        assertTrue(invoice.getSections().isEmpty());
        assertTrue(invoice.render().contains("pedro@correo.com"));
        assertTrue(invoice.render().endsWith("TOTAL: 0.00"));
    }

    @Test
    void groupInvoiceListsCompositionWithoutAddingIt() {
        Group g = new Group("G01", "Familia Garcia");
        Map<String, String> members = new LinkedHashMap<>();
        members.put("ana@correo.com", "12345678Z");
        members.put("pedro@correo.com", "87654321X");
        GroupTicket ticket = GroupTicket.purchase("TG01", g, DAY, new BigDecimal("40.00"), members);
        Invoice invoice = new GroupInvoiceBuilder().forGroup(g).groupTickets(List.of(ticket)).build();
        String text = invoice.render();
        assertTrue(text.contains("G01 - Familia Garcia"));
        assertTrue(text.contains("TG01:12345678Z ana@correo.com (included)"));
        assertEquals(new BigDecimal("80.00"), invoice.total());
        assertEquals(1, invoice.getSections().get(0).items().size());
    }
}
