package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.invoice.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest extends ParkTestSupport {

    private List<String> sectionTitles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void userInvoiceCountsPassesOnComposedTicketsButNotTheTicket() {
        groupWith("G01", 6);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        passes.buySpeedPasses("SP01", "T001", "1");
        tickets.createGroup("TG01", "G01", "2027-04-15");
        passes.buySpecialPass("PE01", "TG01:" + dni(1));
        Invoice invoice = invoices.userInvoice(email(1));
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), sectionTitles(invoice));
        assertEquals(1, invoice.getSections().get(0).lines().size());
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        String text = processor.process("INVOICE CREATE;USER;" + email(1)).output();
        assertTrue(text.contains("DNI: " + dni(1)), text);
        assertFalse(text.contains("Ticket TG01:"), text);
        assertTrue(text.endsWith("TOTAL: 80.00 EUR"), text);
    }

    @Test
    void emptySectionsOmitted() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        assertEquals(List.of("Individual tickets"), sectionTitles(invoices.userInvoice(email(1))));
        user(2);
        Invoice empty = invoices.userInvoice(email(2));
        assertTrue(empty.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), empty.getTotal());
    }

    @Test
    void groupInvoiceTotalsGroupTicketsOnly() {
        groupWith("G01", 6);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        passes.buySpeedPasses("SP01", "TG01:" + dni(2), "10");
        Invoice invoice = invoices.groupInvoice("G01");
        assertEquals(List.of("Group tickets"), sectionTitles(invoice));
        InvoiceLine line = invoice.getSections().get(0).lines().get(0);
        assertEquals(new BigDecimal("192.00"), line.amount());
        assertEquals(6, line.details().size());
        assertEquals(new BigDecimal("192.00"), invoice.getTotal());
        String text = processor.process("INVOICE CREATE;GROUP;G01").output();
        assertTrue(text.contains("Group: G01"), text);
        assertTrue(text.contains("TG01:" + dni(6)), text);
        assertFalse(text.contains("SpeedPass"), text);
    }

    @Test
    void unknownRecipientsAndTypes() {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
        assertTrue(processor.process("INVOICE CREATE;COMPANY;x").output().startsWith("ERROR:"));
    }

    @Test
    void builderStepsProduceOnlyRelevantParts() {
        Group group = groupWith("G01", 1);
        InvoiceBuilder builder = new GroupInvoiceBuilder(park.getDate(), group, List.of());
        Invoice invoice = new InvoiceDirector().construct(builder);
        assertEquals("GROUP INVOICE", invoice.getTitle());
        assertEquals(List.of("Group: G01", "Name: Group G01"), invoice.getRecipient());
        assertTrue(invoice.getSections().isEmpty());
    }
}
