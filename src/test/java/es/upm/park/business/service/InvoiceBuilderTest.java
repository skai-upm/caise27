package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "1", "1234567890123456");
    final User luis = new User("Luis", "Martin", "11111111H", "luis@correo.com", "Clave1!", "1", "1234567890123456");

    private Group group(String id, User... members) {
        Group g = new Group(id, "Grupo " + id);
        for (User u : members) {
            g.add(u);
        }
        return g;
    }

    @Test
    void userInvoiceChargesOnlyStandaloneTickets() {
        GroupTicket tg = GroupTicket.purchase("TG01", group("G01", ana, pedro, luis), DAY, new BigDecimal("40.00"));
        List<IndividualTicket> standalone = List.of(
                new IndividualTicket("T001", ana, DAY, new BigDecimal("40.00")),
                new IndividualTicket("T002", ana, DAY, new BigDecimal("60.00")),
                new IndividualTicket("T003", pedro, DAY, new BigDecimal("40.00")));
        Invoice invoice = new InvoiceDirector().construct(new UserInvoiceBuilder(ana, standalone, List.of()));
        assertTrue(invoice.recipient().contains("ana@correo.com"));
        assertEquals(1, invoice.sections().size());
        assertEquals(2, invoice.sections().get(0).lines().size());
        assertEquals(new BigDecimal("100.00"), invoice.total());
        assertTrue(invoice.sections().get(0).lines().stream().noneMatch(l -> l.description().startsWith("TG01")));
        assertEquals(3, tg.getConstituents().size());
    }

    @Test
    void groupInvoiceUsesStoredTotalsOnly() {
        Group g = group("G01", ana, pedro, luis);
        Group other = group("G02", ana);
        List<GroupTicket> all = List.of(
                GroupTicket.purchase("TG01", g, DAY, new BigDecimal("40.00")),
                GroupTicket.purchase("TG02", g, DAY.plusDays(1), new BigDecimal("40.00")),
                GroupTicket.purchase("TG03", other, DAY, new BigDecimal("40.00")));
        Invoice invoice = new InvoiceDirector().construct(new GroupInvoiceBuilder(g, all));
        assertEquals(1, invoice.sections().size());
        assertEquals(2, invoice.sections().get(0).lines().size());
        assertEquals(new BigDecimal("216.00"), invoice.total());
        assertTrue(invoice.sections().get(0).lines().get(0).description().contains("3 members, 10% discount"));
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice user = new InvoiceDirector().construct(new UserInvoiceBuilder(luis, List.of(), List.of()));
        Invoice grp = new InvoiceDirector().construct(new GroupInvoiceBuilder(group("G01"), List.of()));
        assertTrue(user.sections().isEmpty());
        assertTrue(grp.sections().isEmpty());
        assertEquals(new BigDecimal("0.00"), user.total());
    }

    @Test
    void directorRunsTheSameStepsInOrder() {
        StringBuilder trace = new StringBuilder();
        InvoiceBuilder recording = new InvoiceBuilder() {
            @Override
            public void buildHeader() {
                trace.append("header,");
            }

            @Override
            public void buildIndividualTickets() {
                trace.append("individual,");
            }

            @Override
            public void buildGroupTickets() {
                trace.append("group,");
            }

            @Override
            public void buildPasses() {
                trace.append("passes");
            }
        };
        new InvoiceDirector().construct(recording);
        assertEquals("header,individual,group,passes", trace.toString());
    }
}
