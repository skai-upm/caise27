package com.parque.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.business.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {
    static final LocalDate D = LocalDate.of(2026, 10, 15);
    final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");

    @Test
    void directorRunsStepsInOrder() {
        List<String> calls = new ArrayList<>();
        InvoiceBuilder spy = new InvoiceBuilder() {
            @Override
            public void buildRecipient() {
                calls.add("recipient");
                setRecipient("x");
            }

            @Override
            public void buildIndividualTicketsSection() {
                calls.add("individual");
            }

            @Override
            public void buildGroupTicketsSection() {
                calls.add("group");
            }
        };
        Invoice invoice = new InvoiceDirector().construct(spy);
        assertEquals(List.of("recipient", "individual", "group"), calls);
        assertEquals("x", invoice.getRecipient());
    }

    @Test
    void userInvoiceHasOnlyIndividualTicketsSection() {
        Invoice invoice = new InvoiceDirector().construct(new UserInvoiceBuilder(ana, List.of(
                new IndividualTicket("T001", ana, D, new BigDecimal("40")),
                new IndividualTicket("T002", ana, D.plusDays(1), new BigDecimal("60")))));
        assertEquals("user Ana Garcia Lopez <ana@correo.com>", invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).getTitle());
        assertEquals("T001 (2026-10-15)", invoice.getSections().get(0).getLines().get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
    }

    @Test
    void groupInvoiceChargesGroupTicketTotalsOnly() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        List<IndividualTicket> c = List.of(new IndividualTicket("TG01-1", ana, D, new BigDecimal("40")),
                new IndividualTicket("TG01-2", ana, D, new BigDecimal("40")),
                new IndividualTicket("TG01-3", ana, D, new BigDecimal("40")));
        GroupTicket tg = new GroupTicket("TG01", g, "G01", D, new BigDecimal("40"), new BigDecimal("108"), c);
        Invoice invoice = new InvoiceDirector().construct(new GroupInvoiceBuilder(g, List.of(tg)));
        assertEquals("group G01 - Familia Garcia", invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals("Group tickets", invoice.getSections().get(0).getTitle());
        assertEquals("TG01 (2026-10-15, 3 members)", invoice.getSections().get(0).getLines().get(0).getDescription());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
    }

    @Test
    void emptySectionsOmitted() {
        Invoice user = new InvoiceDirector().construct(new UserInvoiceBuilder(ana, List.of()));
        assertTrue(user.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), user.getTotal());
        Invoice group = new InvoiceDirector().construct(new GroupInvoiceBuilder(new Group("G02", "Vacio"), List.of()));
        assertTrue(group.getSections().isEmpty());
    }
}
