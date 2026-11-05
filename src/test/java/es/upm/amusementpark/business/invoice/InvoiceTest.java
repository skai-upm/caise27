package es.upm.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        Park park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()));
        for (String c : List.of("SYSTEM DATE;2027-04-01",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "SYSTEM PRICE;45.50",
                "TICKET CREATE;T002;ana@correo.com;2027-04-16", "SYSTEM PRICE;40",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "GROUPTICKET CREATE;TG02;G01;2027-04-16")) {
            assertFalse(p.process(c).startsWith("ERROR"), c);
        }
    }

    @Test
    void userInvoiceWithoutDoubleCounting() {
        String inv = p.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(inv.contains("DNI: 12345678Z"));
        assertTrue(inv.contains("T001 (2027-04-15) : 40.00 EUR"));
        assertTrue(inv.contains("T002 (2027-04-16) : 45.50 EUR"));
        assertFalse(inv.contains("TG01:12345678Z"));
        assertTrue(inv.endsWith("TOTAL: 85.50 EUR"), inv);
    }

    @Test
    void groupInvoiceWithComposition() {
        String inv = p.process("INVOICE CREATE;GROUP;G01");
        assertTrue(inv.contains("Group: G01"));
        assertTrue(inv.contains("Name: Familia"));
        assertTrue(inv.contains("TG01 (2027-04-15, 2 members) : 80.00 EUR"));
        assertTrue(inv.contains("TG01:87654321X - pedro@correo.com"));
        assertTrue(inv.endsWith("TOTAL: 160.00 EUR"), inv);
    }

    @Test
    void emptySectionsOmitted() {
        String inv = p.process("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(inv.contains("[Individual tickets]"));
        assertTrue(inv.endsWith("TOTAL: 0.00 EUR"));
    }

    @Test
    void unknownRecipients() {
        assertTrue(p.process("INVOICE CREATE;USER;nadie@correo.com").startsWith("ERROR"));
        assertTrue(p.process("INVOICE CREATE;GROUP;G99").startsWith("ERROR"));
        assertTrue(p.process("INVOICE CREATE;COMPANY;X").startsWith("ERROR"));
    }

    @Test
    void builderStepsAndCommonAbstraction() {
        assertTrue(InvoiceBuilder.class.isAssignableFrom(UserInvoiceBuilder.class));
        assertTrue(InvoiceBuilder.class.isAssignableFrom(GroupInvoiceBuilder.class));
        Invoice empty = new GroupInvoiceBuilder().groupTickets(List.of()).build();
        assertTrue(empty.getSections().isEmpty());
        assertEquals(BigDecimal.ZERO, empty.getTotal());
    }
}
