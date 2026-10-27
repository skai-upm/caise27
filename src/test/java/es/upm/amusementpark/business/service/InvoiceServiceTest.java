package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.InvoiceOperationException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceServiceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private final InvoiceService invoices = new InvoiceService();
    private TicketService tickets;

    @BeforeEach
    void setUp() throws Exception {
        UserRepository userRepository = new UserRepository();
        park.initialize(userRepository, new GroupRepository(), DAY.minusDays(14));
        UserService users = new UserService(userRepository);
        users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        GroupService groups = new GroupService();
        groups.createGroup("G01", "Familia Garcia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
        tickets = new TicketService();
    }

    @Test
    void userInvoiceIncludesOnlyDirectTickets() throws Exception {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        park.setCurrentPrice(new BigDecimal("45.50"));
        tickets.createTicket("T002", "ana@correo.com", DAY);
        tickets.createGroupTicket("TG01", "G01", DAY);
        Invoice invoice = invoices.createUserInvoice("ana@correo.com");
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
        String text = String.join("\n", invoice.render());
        assertTrue(text.contains("T001") && text.contains("T002"));
        assertFalse(text.contains("TG01"));
        assertTrue(text.contains("DNI: 12345678Z"));
    }

    @Test
    void userWithoutTicketsHasNoTicketSection() throws Exception {
        Invoice invoice = invoices.createUserInvoice("pedro@correo.com");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceShowsGroupTicketsAndComposition() throws Exception {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        tickets.createGroupTicket("TG01", "G01", DAY);
        Invoice invoice = invoices.createGroupInvoice("G01");
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
        String text = String.join("\n", invoice.render());
        assertTrue(text.contains("Group: G01 - Familia Garcia"));
        assertTrue(text.contains("TG01:12345678Z") && text.contains("TG01:87654321X"));
        assertFalse(text.contains("T001 "));
    }

    @Test
    void unknownRecipientsAreRejected() {
        assertThrows(InvoiceOperationException.class, () -> invoices.createUserInvoice("nadie@correo.com"));
        assertThrows(InvoiceOperationException.class, () -> invoices.createGroupInvoice("G99"));
    }
}
