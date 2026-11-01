package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvoiceCommandsTest extends ParkTestSupport {

    @Test
    void userInvoiceListsDirectTicketsOnly() {
        createUsers(3);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SYSTEM PRICE;60.00");
        ok("TICKET CREATE;T002;" + email(1) + ";2026-10-16");
        ok("TICKET CREATE;T003;" + email(2) + ";2026-10-16");
        createGroupWithUsers("G01", 3);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");

        String invoice = run("INVOICE CREATE;USER;" + email(1));
        assertTrue(invoice.startsWith("INVOICE - User Name1 Surname 1 (" + email(1) + ")"), invoice);
        assertTrue(invoice.contains("Individual tickets:"), invoice);
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) 40.00"), invoice);
        assertTrue(invoice.contains("Ticket T002 (2026-10-16) 60.00"), invoice);
        assertFalse(invoice.contains("T003"), invoice);
        assertFalse(invoice.contains("TG01"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 100.00"), invoice);
    }

    @Test
    void groupInvoiceListsGroupTicketsOnly() {
        createUsers(3);
        createGroupWithUsers("G01", 3);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("GROUPTICKET CREATE;TG02;G01;2026-10-16");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");

        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE - Group G01 Group G01"), invoice);
        assertTrue(invoice.contains("Group tickets:"), invoice);
        assertTrue(invoice.contains("Group ticket TG01 (2026-10-15, 3 members) 108.00"), invoice);
        assertTrue(invoice.contains("Group ticket TG02 (2026-10-16, 3 members) 108.00"), invoice);
        assertFalse(invoice.contains("TG01-1"), invoice);
        assertFalse(invoice.contains("T001"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 216.00"), invoice);
    }

    @Test
    void recipientWithoutPurchasesHasNoSections() {
        createUsers(1);
        ok("GROUP CREATE;G01;Familia");
        String userInvoice = run("INVOICE CREATE;USER;" + email(1));
        assertEquals(2, userInvoice.lines().count(), userInvoice);
        assertTrue(userInvoice.endsWith("TOTAL: 0.00"), userInvoice);
        assertTrue(run("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 0.00"));
    }

    @Test
    void rejectsUnknownRecipients() {
        error("INVOICE CREATE;USER;nadie@correo.com");
        error("INVOICE CREATE;GROUP;G99");
        error("INVOICE CREATE;SHOP;X");
        error("INVOICE CREATE;USER");
    }
}
