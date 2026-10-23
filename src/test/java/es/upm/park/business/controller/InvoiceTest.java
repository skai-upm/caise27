package es.upm.park.business.controller;

import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.invoice.InvoiceItem;
import es.upm.park.business.invoice.InvoiceSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static es.upm.park.business.controller.ParkFixture.price;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceTest {

    @TempDir
    Path dir;

    private InvoiceController invoices;

    @BeforeEach
    void setUp() throws ParkException {
        ParkFixture.openWithUsers(dir);
        invoices = InvoiceController.getInstance();
        TicketController tickets = TicketController.getInstance();
        PassController passes = PassController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        passes.buySpeedPasses("SP01", "T001", 10);
        passes.buySpecialPass("PE01", "T001");
        passes.buySpeedPasses("SP02", "TG01:12345678Z", 1);
        passes.buySpeedPasses("SP03", "TG01:87654321X", 1);
        passes.buySpeedPasses("SP04", "TG01:11111111H", 1);
    }

    private static List<String> titles(Invoice invoice) {
        return invoice.sections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void anasInvoice() throws ParkException {
        Invoice invoice = invoices.userInvoice("ana@correo.com");

        assertTrue(invoice.recipient().contains("DNI: 12345678Z"));
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), titles(invoice));
        assertEquals(1, invoice.sections().get(0).items().size());
        assertEquals(11, invoice.sections().get(1).items().size());
        assertEquals(price("167.20"), invoice.total());
        for (InvoiceSection section : invoice.sections()) {
            for (InvoiceItem item : section.items()) {
                assertFalse(item.description().startsWith("Ticket TG01:"), "group components are not charged");
            }
        }
    }

    @Test
    void sectionsWithoutItemsAreOmitted() throws ParkException {
        Invoice marta = invoices.userInvoice("marta@correo.com");
        assertTrue(marta.sections().isEmpty());
        assertEquals(price("0.00"), marta.total());

        Invoice pedro = invoices.userInvoice("pedro@correo.com");
        assertEquals(List.of("SpeedPasses"), titles(pedro));
        assertEquals(price("7.20"), pedro.total());
    }

    @Test
    void groupInvoice() throws ParkException {
        Invoice invoice = invoices.groupInvoice("G01");

        assertTrue(invoice.recipient().contains("Group: G01"));
        assertTrue(invoice.recipient().contains("Name: Familia Garcia"));
        assertEquals(List.of("Group tickets"), titles(invoice));
        InvoiceItem tg01 = invoice.sections().get(0).items().get(0);
        assertEquals(price("108.00"), tg01.amount());
        assertEquals(3, tg01.details().size());
        assertEquals(price("108.00"), invoice.total(), "members' passes are not added");
    }

    @Test
    void unknownRecipients() {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nobody@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
    }
}
