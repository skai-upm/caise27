package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.ElementNotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    private final ParkController park = ParkController.getInstance();
    private final InvoiceController invoices = InvoiceController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate("2027-04-01");
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        for (int i = 1; i <= 6; i++) {
            park.registerUser(new User("u" + i + "@correo.com", "0000000" + i + "X"));
        }
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static List<String> sectionTitles(Invoice invoice) {
        return invoice.getSections().stream().map(InvoiceSection::getTitle).toList();
    }

    @Test
    void emptySectionsOmittedAndTotalZero() {
        Invoice invoice = new GroupInvoiceBuilder().addGroup(new Group("G01", "F")).addGroupTickets(List.of()).build();
        assertEquals(List.of("Group"), sectionTitles(invoice));
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void buildersOfferOnlyTheirOwnSteps() {
        List<String> userSteps = Arrays.stream(UserInvoiceBuilder.class.getDeclaredMethods()).map(Method::getName).toList();
        List<String> groupSteps = Arrays.stream(GroupInvoiceBuilder.class.getDeclaredMethods()).map(Method::getName).toList();
        assertFalse(userSteps.contains("addGroupTickets"));
        assertFalse(groupSteps.contains("addIndividualTickets"));
    }

    @Test
    void userInvoiceWithDirectTickets() {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.setBasePrice("45.50");
        park.createIndividualTicket("T002", "ana@correo.com", "2027-04-16");
        Invoice invoice = invoices.createUserInvoice("ana@correo.com");
        assertEquals(new BigDecimal("85.50"), invoice.getTotal());
        String text = invoice.toText();
        assertTrue(text.contains("ana@correo.com") && text.contains("12345678Z"));
        assertTrue(text.contains("T001") && text.contains("T002"));
    }

    @Test
    void groupComponentsNotOnUserInvoice() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        Invoice invoice = invoices.createUserInvoice("ana@correo.com");
        assertEquals(List.of("User"), sectionTitles(invoice));
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceOfSixMembers() throws IOException {
        park.createGroup("G01", "Seis");
        for (int i = 1; i <= 6; i++) {
            park.addUserToGroup("G01", "u" + i + "@correo.com");
        }
        park.createGroupTicket("TG02", "G01", "2027-04-15");
        park.createIndividualTicket("T009", "u1@correo.com", "2027-04-15");
        String out = run("INVOICE CREATE;GROUP;G01\n");
        assertTrue(out.contains("Identifier: G01") && out.contains("Name: Seis"));
        assertTrue(out.contains("Group ticket TG02"));
        assertEquals(6, out.split("TG02:", -1).length - 1);
        assertTrue(out.contains("TOTAL: 192.00 EUR"));
        assertFalse(out.contains("T009"));
        assertEquals(new BigDecimal("192.00"), invoices.createGroupInvoice("G01").getTotal());
    }

    @Test
    void unknownRecipientsAndBadTargets() throws IOException {
        assertThrows(ElementNotFoundException.class, () -> invoices.createUserInvoice("nadie@correo.com"));
        assertThrows(ElementNotFoundException.class, () -> invoices.createGroupInvoice("G99"));
        String out = run("INVOICE CREATE;COMPANY;X\nINVOICE CREATE;USER\nINVOICE CREATE;GROUP;G99\nHELP\n");
        assertTrue(out.contains("Error: invalid invoice target"));
        assertTrue(out.contains("Error: invalid arguments for INVOICE CREATE"));
        assertTrue(out.contains("Error: group not found: G99"));
        assertTrue(out.contains("INVOICE CREATE;USER;<email>"));
        assertTrue(out.contains("INVOICE CREATE;GROUP;<groupIdentifier>"));
    }
}
