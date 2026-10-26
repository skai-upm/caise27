package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import es.upm.parque.business.invoice.TextInvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

import java.io.IOException;
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

    private TicketController tickets;
    private GroupController groups;
    private InvoiceController invoices;

    @BeforeEach
    void setUp() throws IOException {
        SystemDate date = new SystemDate(LocalDate.of(2026, 10, 1));
        UserController users = UserController.getInstance().configure(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        users.create("Luis", "Ruiz", "00000000T", "luis@correo.com", "Clave1!", "600000000", "1111222233334444");
        groups = GroupController.getInstance().configure(new GroupRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "luis@correo.com");
        tickets = TicketController.getInstance().configure(new TicketRepository(dir.resolve("t.txt"), dir.resolve("gt.txt")), users, groups, date);
        invoices = InvoiceController.getInstance().configure(users, groups, tickets, new TextInvoiceBuilder());
    }

    @Test
    void userInvoiceExcludesGroupTickets() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        tickets.createGroupTicket("TG01", "G01", "2026-10-16");
        Invoice invoice = invoices.userInvoice("ana@correo.com");
        assertEquals(List.of(new InvoiceLine("T001 2026-10-15", new BigDecimal("40.00"))),
                invoice.getSections().get(0).lines());
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceListsGroupTicketsOnly() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        tickets.createGroupTicket("TG01", "G01", "2026-10-16");
        Invoice invoice = invoices.groupInvoice("G01");
        assertEquals(1, invoice.getSections().size());
        assertEquals(List.of(new InvoiceLine("TG01 2026-10-16 3 members -10%", new BigDecimal("108.00"))),
                invoice.getSections().get(0).lines());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void emptyInvoicesAndUnknownRecipients() {
        Invoice invoice = invoices.userInvoice("luis@correo.com");
        assertTrue(invoice.getSections().isEmpty());
        assertTrue(invoice.toLines().contains("No billable items"));
        assertTrue(invoices.groupInvoice("G01").getSections().isEmpty());
        assertThrows(BusinessException.class, () -> invoices.userInvoice("nadie@correo.com"));
        assertThrows(BusinessException.class, () -> invoices.groupInvoice("G99"));
    }
}
