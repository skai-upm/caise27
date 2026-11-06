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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerInvoiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
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
        Invoice invoice = park.userInvoice(ANA);
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
        Invoice invoice = park.groupInvoice("G01");
        String text = invoice.render();
        assertTrue(text.contains("TG01") && text.contains("TG02"));
        assertTrue(text.contains("TG02:87654321X"));
        assertFalse(text.contains("T001 "));
        assertEquals(new BigDecimal("171.00"), invoice.total());
    }

    @Test
    void userWithoutPurchasesHasZeroTotal() {
        Invoice invoice = park.userInvoice(PEDRO);
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.total());
    }

    @Test
    void unknownRecipientsAreRejected() {
        assertThrows(NotFoundException.class, () -> park.userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> park.groupInvoice("G99"));
    }
}
