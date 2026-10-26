package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.parque.business.invoice.DefaultInvoiceBuilder;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceControllerTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private TicketController tickets;
    private GroupController groups;
    private InvoiceController invoices;

    @BeforeEach
    void setUp() {
        SystemDate date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        UserController users = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        users.create("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        groups = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(new FileTicketRepository(dir.resolve("tickets.txt"),
                dir.resolve("grouptickets.txt")), users, groups, date);
        invoices = InvoiceController.getInstance().configure(users, groups, tickets, date, new DefaultInvoiceBuilder(),
                new InvoiceDirector());
        groups.create("G01", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "luis@correo.com");
    }

    @Test
    void userInvoiceExcludesTicketsInsideGroupTickets() {
        tickets.create("T001", "ana@correo.com", OCT_15);
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        Invoice invoice = invoices.userInvoice("ana@correo.com");
        assertEquals(1, invoice.getSections().size());
        assertEquals(1, invoice.getSections().get(0).getLines().size());
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());
        assertThrows(IllegalArgumentException.class, () -> invoices.userInvoice("nadie@correo.com"));
    }

    @Test
    void groupInvoiceListsOnlyGroupTickets() {
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        tickets.createGroupTicket("TG02", "G01", OCT_15.plusDays(1));
        Invoice invoice = invoices.groupInvoice("G01");
        assertEquals(2, invoice.getSections().get(0).getLines().size());
        assertEquals(new BigDecimal("216.00"), invoice.getTotal());
        assertThrows(IllegalArgumentException.class, () -> invoices.groupInvoice("G99"));
    }
}
