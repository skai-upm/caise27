package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.model.GroupTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceTest extends ParkTestSupport {

    private InvoiceController invoices;
    private PassController passes;

    @BeforeEach
    void controllers() {
        invoices = InvoiceController.getInstance();
        passes = PassController.getInstance();
    }

    private List<String> sectionTitles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    /** Ana (user 1) with T001 + SpeedPass and a composition ticket of TG01 with a Special Pass. */
    private GroupTicket scenario() {
        for (int i = 1; i <= 3; i++) {
            user(i);
        }
        park.createTicket("T001", email(1), "2027-04-15");
        passes.buySpeedPasses("SP01", "T001", "1");
        park.createGroup("G01", "Familia Garcia");
        for (int i = 1; i <= 3; i++) {
            park.addToGroup("G01", email(i));
        }
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        passes.buySpecialPass("PE01", "TG01:" + dni(1));
        return tg;
    }

    @Test
    void userInvoice() {
        scenario();
        Invoice invoice = invoices.create("USER", email(1));
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), sectionTitles(invoice));
        assertEquals(1, invoice.getSections().get(0).items().size(), "composition ticket not counted as purchase");
        assertEquals(new BigDecimal("84.00"), invoice.getTotal());
        String text = invoice.render();
        assertTrue(text.contains("DNI: " + dni(1)));
        assertTrue(text.contains("Ticket T001"));
        assertTrue(text.contains("SpeedPass SP01:1"));
        assertTrue(text.contains("Special Pass PE01 for ticket TG01:" + dni(1)));
        assertFalse(text.contains("Ticket TG01:"));
        assertTrue(text.contains("TOTAL: 84.00 EUR"));
    }

    @Test
    void emptySectionsOmitted() {
        user(1);
        park.createTicket("T001", email(1), "2027-04-15");
        Invoice invoice = invoices.create("USER", email(1));
        assertEquals(List.of("Individual tickets"), sectionTitles(invoice));
        assertFalse(invoice.render().contains("SpeedPasses"));

        user(2);
        Invoice empty = invoices.create("USER", email(2));
        assertTrue(empty.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), empty.getTotal());
    }

    @Test
    void groupInvoice() {
        scenario();
        Invoice invoice = invoices.create("GROUP", "G01");
        assertEquals(List.of("Group tickets"), sectionTitles(invoice));
        assertEquals(new BigDecimal("108.00"), invoice.getTotal(), "member passes not added");
        assertEquals(3, invoice.getSections().get(0).items().get(0).details().size());
        String text = invoice.render();
        assertTrue(text.contains("Group: G01"));
        assertTrue(text.contains("Name: Familia Garcia"));
        assertTrue(text.contains("Group ticket TG01"));
        assertTrue(text.contains("TG01:" + dni(2)));
        assertTrue(text.contains("TOTAL: 108.00 EUR"));
    }

    @Test
    void invalidRecipients() {
        assertThrows(ParkException.class, () -> invoices.create("USER", "nadie@correo.com"));
        assertThrows(ParkException.class, () -> invoices.create("GROUP", "G99"));
        assertThrows(ParkException.class, () -> invoices.create("FOO", "x"));
    }
}
