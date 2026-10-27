package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.controller.ApplicationInitializer;

class InvoiceControllerTest {

    private InvoiceController invoices;

    @BeforeEach
    void setUp() throws Exception {
        ParkController park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        park.setCurrentDate(LocalDate.of(2027, 4, 10));
        UserController users = UserController.getInstance();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        TicketController tickets = TicketController.getInstance();
        tickets.createGroupTicket("TG01", "G01", LocalDate.of(2027, 4, 15));
        tickets.createIndividual("T001", "ana@correo.com", LocalDate.of(2027, 4, 15));
        invoices = InvoiceController.getInstance();
    }

    @Test
    void userInvoiceExcludesGroupComponents() throws Exception {
        Invoice invoice = invoices.userInvoice("ana@correo.com");
        assertEquals(1, invoice.getSections().get(0).items().size());
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());
        assertEquals(0, invoices.userInvoice("pedro@correo.com").getSections().size());
    }

    @Test
    void groupInvoiceCountsGroupTicketOnce() throws Exception {
        Invoice invoice = invoices.groupInvoice("G01");
        InvoiceItem item = invoice.getSections().get(0).items().get(0);
        assertEquals(2, item.details().size());
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
    }

    @Test
    void unknownRecipients() {
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("x@correo.com"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
    }
}
