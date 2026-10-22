package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.InvoiceSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private InvoiceController invoices;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        invoices = park.getInvoiceController();
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        park.getPassController().buySpeedPasses("SP01", "T001", "10");
        park.getPassController().buySpecialPass("PE01", "T001");
        park.getPassController().buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.getPassController().buySpeedPasses("SP03", "TG01:87654321X", "1");
    }

    private static List<String> titles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::title).toList();
    }

    @Test
    void userInvoiceHasPersonalPurchasesOnly() {
        Invoice invoice = invoices.create("USER", "ana@correo.com");

        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"), titles(invoice));
        assertEquals(1, invoice.getSections().get(0).lines().size());
        assertEquals(11, invoice.getSections().get(1).lines().size());
        assertEquals(new BigDecimal("167.20"), invoice.getTotal());
        assertTrue(invoice.getRecipient().contains("DNI: 12345678Z"));
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice pedro = invoices.create("user", "pedro@correo.com");
        Invoice marta = invoices.create("USER", "marta@correo.com");

        assertEquals(List.of("SpeedPasses"), titles(pedro));
        assertEquals(new BigDecimal("7.20"), pedro.getTotal());
        assertTrue(marta.getSections().isEmpty());
        assertEquals(BigDecimal.ZERO, marta.getTotal());
    }

    @Test
    void groupInvoiceCountsGroupTicketsOnce() {
        Invoice invoice = invoices.create("GROUP", "G01");

        assertEquals(List.of("Group tickets"), titles(invoice));
        assertEquals(3, invoice.getSections().get(0).lines().get(0).details().size());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertTrue(invoice.getRecipient().contains("Name: Familia Garcia"));
    }

    @Test
    void rejectsUnknownRecipients() {
        assertThrows(NotFoundException.class, () -> invoices.create("USER", "nobody@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.create("GROUP", "G99"));
        assertThrows(ValidationException.class, () -> invoices.create("COMPANY", "X"));
    }
}
