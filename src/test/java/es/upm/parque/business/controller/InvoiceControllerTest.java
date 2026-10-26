package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceControllerTest {

    @TempDir
    Path dir;

    private TestPark park;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.users.create("Luis", "Perez", "11111111H", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 1));
        park.groups.create("G01", "Familia");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        park.groups.addMember("G01", "luis@correo.com");
    }

    @Test
    void userInvoiceListsDirectTicketsOnly() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.controller.setTicketPrice(new BigDecimal("60.00"));
        park.tickets.create("T002", "ana@correo.com", "2026-10-20");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");

        Invoice invoice = park.invoices.forUser("ana@correo.com");
        assertEquals("Ana Garcia (ana@correo.com)", invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals(List.of(new InvoiceLine("Ticket T001 2026-10-15", new BigDecimal("40.00")),
                new InvoiceLine("Ticket T002 2026-10-20", new BigDecimal("60.00"))), invoice.getSections().get(0).lines());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
    }

    @Test
    void memberOnlyCoveredByGroupTicketHasEmptyInvoice() {
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        Invoice invoice = park.invoices.forUser("pedro@correo.com");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceListsGroupTicketsWithoutInternalTickets() {
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.groups.create("G02", "Pareja");
        park.groups.addMember("G02", "ana@correo.com");
        park.groups.addMember("G02", "pedro@correo.com");
        park.tickets.createGroupTicket("TG02", "G02", "2026-10-16");

        Invoice invoice = park.invoices.forGroup("G01");
        assertEquals("Group G01 - Familia", invoice.getRecipient());
        assertEquals(List.of(new InvoiceLine("Group ticket TG01 2026-10-15 (3 members)", new BigDecimal("108.00"))),
                invoice.getSections().get(0).lines());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertEquals(1, park.invoices.forGroup("G02").getSections().get(0).lines().size());
    }

    @Test
    void unknownRecipientsAreRejected() {
        assertThrows(ParkException.class, () -> park.invoices.forUser("nadie@correo.com"));
        assertThrows(ParkException.class, () -> park.invoices.forGroup("G99"));
        assertTrue(park.invoices.forGroup("G01").getSections().isEmpty());
    }
}
