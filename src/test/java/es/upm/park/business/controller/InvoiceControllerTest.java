package es.upm.park.business.controller;

import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.invoice.Invoice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";

    private final ParkController park = ParkController.getInstance();
    private final InvoiceController invoices = InvoiceController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        invoices.setParkController(park);
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.withDnis(Map.of(ANA, "12345678Z", PEDRO, "87654321X")));
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", ANA);
        park.addUserToGroup("G01", PEDRO);
    }

    @AfterEach
    void tearDown() {
        park.reset();
    }

    @Test
    void userInvoiceCountsOnlyDirectTickets() {
        park.createIndividualTicket("T001", ANA, TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        park.createIndividualTicket("T002", ANA, TODAY.plusDays(1));
        park.setBasePrice(new BigDecimal("40.00"));
        park.createGroupTicket("TG01", "G01", TODAY);
        Invoice invoice = invoices.userInvoice(ANA);
        String text = invoice.render();
        assertTrue(text.contains("ana@correo.com"));
        assertTrue(text.contains("12345678Z"));
        assertTrue(text.contains("T001") && text.contains("T002"));
        assertFalse(text.contains("TG01:12345678Z"));
        assertEquals(new BigDecimal("85.50"), invoice.total());
    }

    @Test
    void groupInvoiceSumsGroupTicketTotals() {
        park.createGroupTicket("TG01", "G01", TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        park.createGroupTicket("TG02", "G01", TODAY.plusDays(1));
        park.createIndividualTicket("T001", ANA, TODAY);
        Invoice invoice = invoices.groupInvoice("G01");
        String text = invoice.render();
        assertTrue(text.contains("TG01") && text.contains("TG02"));
        assertTrue(text.contains("TG02:87654321X"));
        assertFalse(text.contains("T001 "));
        assertEquals(new BigDecimal("171.00"), invoice.total());
    }

    @Test
    void userWithoutPurchasesHasZeroTotal() {
        Invoice invoice = invoices.userInvoice(PEDRO);
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.total());
    }

    @Test
    void singleInstance() {
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void unconfiguredCollaboratorIsReported() {
        invoices.setParkController(null);
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> invoices.groupInvoice("G01"));
        assertTrue(error.getMessage().contains("not configured"));
    }

    @Test
    void unknownRecipientsAreRejected() {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
    }
}
