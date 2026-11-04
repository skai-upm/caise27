package es.upm.etsisi.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
    private final User luis = new User("Luis", "M", "11111111H", "luis@correo.com", "Luis3$", "3", "1111222233334444");
    private final InvoiceDirector director = new InvoiceDirector();

    @Test
    void userInvoiceListsDirectTicketsWithCapturedPrices() {
        Invoice invoice = director.construct(new UserInvoiceBuilder(ana, List.of(
                new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00")),
                new IndividualTicket("T002", DAY.plusDays(1), ana, new BigDecimal("60.00")))));
        assertTrue(invoice.getTitle().contains("ana@correo.com"));
        assertEquals(1, invoice.getSections().size());
        InvoiceSection section = invoice.getSections().get(0);
        assertEquals("Individual tickets", section.getTitle());
        assertEquals("T001 2026-10-15", section.getLines().get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceListsGroupTicketTotalsOnly() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        g.addMember(luis);
        GroupTicket tg = new GroupTicket("TG01", DAY, g, new BigDecimal("40.00"));
        Invoice invoice = director.construct(new GroupInvoiceBuilder(g, List.of(tg)));
        assertEquals("Invoice for group G01 (Familia Garcia)", invoice.getTitle());
        assertEquals(1, invoice.getSections().get(0).getLines().size());
        assertEquals("TG01 2026-10-15 (3 members, 10% discount)", invoice.getSections().get(0).getLines().get(0).getDescription());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void emptySectionsAreOmittedAndTotalIsZero() {
        Invoice user = director.construct(new UserInvoiceBuilder(pedro, List.of()));
        assertTrue(user.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), user.getTotal());
        Invoice group = director.construct(new GroupInvoiceBuilder(new Group("G02", "Vacio"), List.of()));
        assertTrue(group.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), group.getTotal());
    }

    @Test
    void directorRunsTheSameStepsForEveryBuilder() {
        List<String> steps = new ArrayList<>();
        InvoiceBuilder recording = new InvoiceBuilder() {
            @Override
            public void buildHeader() {
                steps.add("header");
                setTitle("t");
            }

            @Override
            public void buildTicketSection() {
                steps.add("tickets");
                addSection("x", List.of(new InvoiceLine("a", new BigDecimal("1.50"))));
            }

            @Override
            public void buildTotal() {
                steps.add("total");
                super.buildTotal();
            }
        };
        Invoice invoice = director.construct(recording);
        assertEquals(List.of("header", "tickets", "total"), steps);
        assertEquals(new BigDecimal("1.50"), invoice.getTotal());
    }

    @Test
    void productIsImmutable() {
        Invoice invoice = director.construct(new UserInvoiceBuilder(ana,
                List.of(new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00")))));
        assertThrows(UnsupportedOperationException.class, () -> invoice.getSections().clear());
        assertThrows(UnsupportedOperationException.class, () -> invoice.getSections().get(0).getLines().clear());
    }
}
