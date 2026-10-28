package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;

class InvoiceServiceTest {

    private final ParkController park = ParkController.getInstance();
    private final LocalDate today = LocalDate.of(2027, 4, 15);

    @BeforeEach
    void setUp() throws Exception {
        park.initialize(new InMemoryUserStorage());
        park.setCurrentDate(today);
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users().createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.groups().createGroup("G01", "Familia");
        park.groups().createGroup("G02", "Vacio");
        park.groups().addMember("G01", "ana@correo.com");
        park.groups().addMember("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", today);
    }

    @Test
    void userInvoiceChargesOnlyDirectTickets() throws Exception {
        Invoice onlyGroup = park.invoices().userInvoice("ana@correo.com");
        assertTrue(onlyGroup.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), onlyGroup.getTotal());
        park.tickets().createIndividualTicket("T001", "ana@correo.com", today);
        park.setBasePrice(new BigDecimal("45.50"));
        park.tickets().createIndividualTicket("T002", "ana@correo.com", today.plusDays(1));
        park.tickets().createIndividualTicket("T003", "pedro@correo.com", today);
        Invoice invoice = park.invoices().userInvoice("ana@correo.com");
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
        assertEquals(2, invoice.getSections().get(0).items().size());
    }

    @Test
    void groupInvoiceListsItsGroupTickets() throws Exception {
        Invoice invoice = park.invoices().groupInvoice("G01");
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        assertTrue(invoice.getHeader().get(0).contains("G01 - Familia"));
        Invoice empty = park.invoices().groupInvoice("G02");
        assertTrue(empty.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), empty.getTotal());
    }

    @Test
    void unknownRecipients() {
        assertThrows(NotFoundException.class, () -> park.invoices().userInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> park.invoices().groupInvoice("G99"));
    }
}
