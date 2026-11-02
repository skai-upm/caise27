package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.InvoiceSection;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceTest extends ParkTestSupport {

    private static List<String> titles(Invoice i) {
        return i.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void userInvoiceCountsDirectTicketsAndPersonalPassesOnly() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        tickets.create("T001", "ana@correo.com", TODAY_S);
        GroupTicket tg = tickets.createGroupTicket("TG01", "G01", TODAY_S);
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpecialPass("PE01", tg.getComposition().get(0).getId());

        Invoice inv = invoices.userInvoice("ana@correo.com");
        assertEquals(List.of("Tickets", "SpeedPasses", "Special Passes"), titles(inv));
        assertEquals(new BigDecimal("88.00"), inv.getTotal());
        String text = inv.render();
        assertTrue(text.contains("Ticket T001"));
        assertFalse(text.contains("Ticket TG01"), "constituent not invoiced to the user");
        assertTrue(text.contains("DNI: 12345678Z"));
        assertTrue(text.endsWith("TOTAL: 88.00 EUR"));
    }

    @Test
    void emptySectionsAreOmitted() {
        ana();
        tickets.create("T001", "ana@correo.com", TODAY_S);
        assertEquals(List.of("Tickets"), titles(invoices.userInvoice("ana@correo.com")));
        pedro();
        Invoice none = invoices.userInvoice("pedro@correo.com");
        assertTrue(none.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), none.getTotal());
    }

    @Test
    void groupInvoiceCountsGroupTicketOnce() {
        groupOf("G03", 3);
        GroupTicket tg = tickets.createGroupTicket("TG03", "G03", TODAY_S);
        passes.createSpeedPasses("SP", tg.getComposition().get(0).getId(), "10");
        Invoice inv = invoices.groupInvoice("G03");
        assertEquals(List.of("Group tickets"), titles(inv));
        assertEquals(new BigDecimal("108.00"), inv.getTotal());
        String text = inv.render();
        assertTrue(text.contains("Group: G03 - Group G03"));
        for (IndividualTicket c : tg.getComposition()) {
            assertTrue(text.contains(c.getId()), "composition shown");
        }
        assertFalse(text.contains("SpeedPass"));
    }

    @Test
    void groupWithoutPurchasesHasNoSections() {
        groups.create("G00", "Vacio");
        assertTrue(invoices.groupInvoice("G00").getSections().isEmpty());
    }

    @Test
    void unknownRecipientsRejected() {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
    }
}
