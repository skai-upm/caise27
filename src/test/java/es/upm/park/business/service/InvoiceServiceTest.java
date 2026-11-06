package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.invoice.Invoice;
import es.upm.park.persistence.repository.UserRepository;

class InvoiceServiceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private InvoiceService invoices;

    @BeforeEach
    void setUp() {
        park.start(new UserRepository(), DAY);
        UserService users = park.getUserService();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        users.create("Luis", "Martin", "00000000T", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        park.getGroupService().create("G01", "Familia Garcia");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.getGroupService().addUser("G01", "luis@correo.com");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.setBasePrice("45.50");
        park.getTicketService().createIndividual("T002", "ana@correo.com", DAY);
        park.setBasePrice("40");
        park.createGroupTicket("TG01", "G01", DAY);
        invoices = park.getInvoiceService();
    }

    @Test
    void userInvoiceListsOnlyDirectTickets() {
        Invoice inv = invoices.userInvoice("ana@correo.com");
        String text = inv.render();
        assertTrue(text.contains("Ana Garcia Lopez"));
        assertTrue(text.contains("DNI: 12345678Z"));
        assertTrue(text.contains("Ticket T001 (2027-04-15)  40.00"));
        assertTrue(text.contains("Ticket T002 (2027-04-15)  45.50"));
        assertFalse(text.contains("TG01"));
        assertEquals(new BigDecimal("85.50"), inv.getTotal());
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        Invoice inv = invoices.userInvoice("pedro@correo.com");
        assertTrue(inv.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), inv.getTotal());
    }

    @Test
    void groupInvoiceCountsGroupTicketsOnce() {
        Invoice inv = invoices.groupInvoice("G01");
        String text = inv.render();
        assertTrue(text.contains("Group G01 - Familia Garcia"));
        assertTrue(text.contains("Group ticket TG01 (2027-04-15, 3 members)  108.00"));
        assertTrue(text.contains("TG01:12345678Z ana@correo.com 36.00"));
        assertTrue(text.contains("TG01:00000000T luis@correo.com 36.00"));
        assertFalse(text.contains("T001"));
        assertEquals(new BigDecimal("108.00"), inv.getTotal());
        assertEquals(1, inv.getSections().size());
    }

    @Test
    void unknownRecipientsRejected() {
        assertEquals("no user with email nadie@correo.com",
                assertThrows(ParkException.class, () -> invoices.userInvoice("nadie@correo.com")).getMessage());
        assertEquals("no group G99", assertThrows(ParkException.class, () -> invoices.groupInvoice("G99")).getMessage());
    }
}
