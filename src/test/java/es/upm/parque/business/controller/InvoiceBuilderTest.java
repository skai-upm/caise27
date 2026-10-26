package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private final InvoiceDirector director = new InvoiceDirector();
    private User ana;
    private User pedro;
    private Group family;
    private List<Ticket> tickets;

    @BeforeEach
    void setUp() {
        ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        family = new Group("G01", "Familia");
        family.addMember(ana);
        family.addMember(pedro);
        tickets = new ArrayList<>();
        tickets.add(new IndividualTicket("T001", DAY, PRICE, ana));
        tickets.add(new IndividualTicket("T002", DAY.plusDays(1), new BigDecimal("60.00"), ana));
        tickets.add(new IndividualTicket("T003", DAY, PRICE, pedro));
        tickets.add(new GroupTicket("TG01", DAY, family, PRICE));
    }

    @Test
    void userInvoiceListsOnlyDirectPurchases() {
        Invoice invoice = director.construct(new UserInvoiceBuilder(ana, tickets), DAY);
        assertTrue(invoice.getRecipient().contains("ana@correo.com"));
        assertEquals(DAY, invoice.getIssueDate());
        assertEquals(1, invoice.getSections().size());
        List<String> lines = invoice.getSections().get(0).getLines().stream().map(InvoiceLine::getDescription).toList();
        assertEquals(List.of("Ticket T001 for 2026-10-15", "Ticket T002 for 2026-10-16"), lines);
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceListsGroupTicketsOnly() {
        Group other = new Group("G02", "Otros");
        other.addMember(pedro);
        tickets.add(new GroupTicket("TG02", DAY, other, PRICE));
        Invoice invoice = director.construct(new GroupInvoiceBuilder(family, tickets), DAY);
        assertEquals("Group G01 Familia", invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals("Group tickets", invoice.getSections().get(0).getTitle());
        assertEquals(1, invoice.getSections().get(0).getLines().size());
        assertTrue(invoice.getSections().get(0).getLines().get(0).getDescription().startsWith("Group ticket TG01"));
        assertEquals(new BigDecimal("80.00"), invoice.getTotal());
    }

    @Test
    void recipientsWithoutPurchasesGetNoSections() {
        User luis = new User("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave3$", "600", "1111222233334444");
        Invoice invoice = director.construct(new UserInvoiceBuilder(luis, tickets), DAY);
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.getTotal()));
    }

    @Test
    void builderCanBeReusedAfterReset() {
        UserInvoiceBuilder builder = new UserInvoiceBuilder(ana, tickets);
        director.construct(builder, DAY);
        Invoice second = director.construct(builder, DAY.plusDays(1));
        assertEquals(1, second.getSections().size());
        assertEquals(DAY.plusDays(1), second.getIssueDate());
    }
}
