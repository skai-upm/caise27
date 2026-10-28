package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerInvoiceTest {

    private final ParkController controller = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.addUser(new User("87654321X", "pedro@correo.com"));
        controller.addUser(new User("11111111H", "luis@correo.com"));
        controller.createGroup("G01", "Familia Garcia");
        for (String email : new String[] {"ana@correo.com", "pedro@correo.com", "luis@correo.com"}) {
            controller.addUserToGroup("G01", email);
        }
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
    }

    @Test
    void userInvoiceExcludesComposedTickets() {
        Invoice onlyGroup = controller.createUserInvoice("ana@correo.com");
        assertTrue(onlyGroup.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), onlyGroup.getTotal());
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.setCurrentPrice("45.50");
        controller.createIndividualTicket("T002", "ana@correo.com", "2027-04-16");
        assertEquals(new BigDecimal("85.50"), controller.createUserInvoice("ana@correo.com").getTotal());
    }

    @Test
    void groupInvoiceTotals() {
        controller.createGroupTicket("TG02", "G01", "2027-04-16");
        assertEquals(new BigDecimal("216.00"), controller.createGroupInvoice("G01").getTotal());
    }

    @Test
    void recipientsMustExist() {
        assertThrows(NotFoundException.class, () -> controller.createUserInvoice("nadie@correo.com"));
        assertThrows(NotFoundException.class, () -> controller.createGroupInvoice("G99"));
    }
}
