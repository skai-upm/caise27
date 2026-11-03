package es.upm.etsisi.park;

import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.UserInvoiceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, ParkFixture.LUIS,
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15");
    }

    @Test
    void userInvoiceCountsDirectTicketsAndPersonalPasses() {
        f.run("SPEEDPASS CREATE;SP01;TG01:12345678Z;1", "SPECIALPASS CREATE;PE01;T001");
        String invoice = f.run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.startsWith("=== USER INVOICE ==="));
        assertTrue(invoice.contains("DNI: 12345678Z"));
        assertTrue(invoice.contains("TICKET T001 (2027-04-15): 40.00 EUR"));
        assertFalse(invoice.contains("TICKET TG01:12345678Z"));
        assertTrue(invoice.contains("SPEEDPASS SP01:1 for ticket TG01:12345678Z (unused): 7.20 EUR"));
        assertTrue(invoice.contains("SPECIAL PASS PE01 for ticket T001 (unused): 40.00 EUR"));
        assertTrue(invoice.endsWith("TOTAL: 87.20 EUR"));
    }

    @Test
    void userInvoiceOmitsEmptySections() {
        String invoice = f.run("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("-- TICKETS --"));
        assertFalse(invoice.contains("SPEEDPASSES"));
        assertFalse(invoice.contains("SPECIAL PASSES"));
        assertTrue(invoice.endsWith("TOTAL: 0.00 EUR"));
        assertTrue(f.run("INVOICE CREATE;USER;ana@correo.com").contains("-- TICKETS --"));
    }

    @Test
    void groupInvoiceCountsGroupTicketOnce() {
        f.run("SPEEDPASS CREATE;SP01;TG01:12345678Z;10");
        String invoice = f.run("INVOICE CREATE;GROUP;G01");
        assertTrue(invoice.startsWith("=== GROUP INVOICE ==="));
        assertTrue(invoice.contains("GROUP: G01") && invoice.contains("NAME: Familia Garcia"));
        assertTrue(invoice.contains("GROUPTICKET TG01 (2027-04-15, 3 members): 108.00 EUR"));
        assertTrue(invoice.contains("    TG01:87654321X pedro@correo.com 36.00 EUR"));
        assertFalse(invoice.contains("SPEEDPASS"));
        assertTrue(invoice.endsWith("TOTAL: 108.00 EUR"));
    }

    @Test
    void invoiceErrors() {
        assertTrue(f.run("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR: no user"));
        assertTrue(f.run("INVOICE CREATE;GROUP;G99").startsWith("ERROR: no group"));
        assertTrue(f.run("INVOICE CREATE;OTHER;x").startsWith("ERROR: invalid invoice type"));
        assertTrue(f.run("INVOICE CREATE;USER").startsWith("ERROR: wrong number of parameters"));
    }

    @Test
    void builderSkipsEmptySectionsAndDoesNotCountDetails() {
        Invoice invoice = new UserInvoiceBuilder().title("T").section("EMPTY").section("A")
                .item("x", new BigDecimal("1.50")).detail("info").section("B").item("y", new BigDecimal("2.00")).build();
        assertEquals(2, invoice.getSections().size());
        assertEquals(new BigDecimal("3.50"), invoice.getTotal());
        Invoice implicit = new UserInvoiceBuilder().item("z", BigDecimal.ONE).build();
        assertEquals("ITEMS", implicit.getSections().get(0).title());
    }
}
