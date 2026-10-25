package es.upm.parque;

import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceSection;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InvoiceTest extends ParkTestSupport {

    @BeforeEach
    void purchases() {
        createAna();
        createPedro();
        String[] others = createUsers(1);
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + ANA);
        ok("GROUP ADD;G01;" + PEDRO);
        ok("GROUP ADD;G01;" + others[0]);
        ok("SYSTEM DATE;2026-10-01");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SPEEDPASS CREATE;T001;1");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPECIALPASS CREATE;TG01-1");
    }

    @Test
    public void userInvoiceListsDirectTicketsAndAllPersonalPasses() {
        String invoice = run("INVOICE CREATE;USER;" + ANA);
        assertTrue(invoice.startsWith("INVOICE USER Ana Garcia Lopez (ana@correo.com)"), invoice);
        assertTrue(invoice.contains("Individual tickets:\n  Ticket T001 for 2026-10-15: 40.00".replace("\n", System.lineSeparator())), invoice);
        assertTrue(invoice.contains("SpeedPass T001-SP1 (ticket T001): 8.00"), invoice);
        assertTrue(invoice.contains("Special Pass TG01-1-SPECIAL (ticket TG01-1): 40.00"), invoice);
        assertFalse(invoice.contains("Ticket TG01-1 for"), "group-generated tickets are not individual purchases");
        assertFalse(invoice.contains("Group ticket"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 88.00"), invoice);
    }

    @Test
    public void groupInvoiceListsOnlyGroupTickets() {
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("INVOICE GROUP G01 Familia"), invoice);
        assertTrue(invoice.contains("Group ticket TG01 for 2026-10-15 (3 members): 108.00"), invoice);
        assertFalse(invoice.contains("Pass"), invoice);
        assertFalse(invoice.contains("Individual tickets"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 108.00"), invoice);
    }

    @Test
    public void emptySectionsAreOmitted() {
        String invoice = run("INVOICE CREATE;user;" + PEDRO);
        assertFalse(invoice.contains("Individual tickets"), invoice);
        assertFalse(invoice.contains("Personal passes"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 0.00"), invoice);
    }

    @Test
    public void unknownRecipientsAreRejected() {
        fails("INVOICE CREATE;USER;nadie@correo.com");
        fails("INVOICE CREATE;GROUP;G99");
        fails("INVOICE CREATE;SHOP;X");
    }

    @Test
    public void builderAddsSectionsStepByStep() {
        Invoice invoice = new StandardInvoiceBuilder()
                .start("R")
                .addIndividualTickets(List.of(tickets().findIndividual("T001")))
                .addPasses(List.of())
                .addGroupTickets(List.of())
                .build();
        assertEquals("R", invoice.recipient());
        assertEquals(List.of("Individual tickets"), invoice.sections().stream().map(InvoiceSection::title).toList());
        assertEquals("40.00", invoice.total().toPlainString());
    }

    @Test
    public void invoicesAreNotPersisted() {
        run("INVOICE CREATE;USER;" + ANA);
        assertTrue(dataDir.toFile().list((dir, name) -> name.toLowerCase().contains("invoice")).length == 0);
    }
}
