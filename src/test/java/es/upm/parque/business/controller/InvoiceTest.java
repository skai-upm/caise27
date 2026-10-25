package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.dni;
import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.TestContext;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceBuilder;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.invoice.InvoiceSection;
import es.upm.parque.business.invoice.StandardInvoiceBuilder;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class InvoiceTest {

    @TempDir
    Path dir;

    private TestContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ctx.group("G01", 3);
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(ctx.run("SPECIALPASS CREATE;TG01-" + dni(1)));
    }

    private static List<String> titles(Invoice invoice) {
        List<String> titles = new ArrayList<>();
        for (InvoiceSection section : invoice.getSections()) {
            titles.add(section.title());
        }
        return titles;
    }

    @Test
    void userInvoiceListsDirectTicketsAndAllPassesOnce() {
        Invoice invoice = ctx.invoices().userInvoice(email(1));
        assertEquals(List.of("Individual tickets", "Personal passes"), titles(invoice));
        assertEquals(1, invoice.getSections().get(0).lines().size());
        assertEquals(2, invoice.getSections().get(1).lines().size());
        assertEquals(new BigDecimal("88.00"), invoice.getTotal());
        String text = ctx.run("INVOICE CREATE;USER;" + email(1));
        assertTrue(text.startsWith("OK INVOICE USER " + email(1)), text);
        assertTrue(text.contains("Ticket T001 (2026-10-15) 40.00"));
        assertTrue(text.contains("SpeedPass T001-SP1 8.00"));
        assertTrue(text.contains("Special Pass TG01-" + dni(1) + "-SPECIAL 40.00"));
        assertFalse(text.contains("Ticket TG01-"));
        assertTrue(text.contains("TOTAL: 88.00"));
    }

    @Test
    void groupInvoiceListsOnlyGroupTickets() {
        Invoice invoice = ctx.invoices().groupInvoice("G01");
        assertEquals(List.of("Group tickets"), titles(invoice));
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        String text = ctx.run("INVOICE CREATE;GROUP;G01");
        assertTrue(text.contains("Group ticket TG01 (2026-10-15, 3 members) 108.00"));
        assertFalse(text.contains("SpeedPass") || text.contains("Special Pass") || text.contains("Ticket T001"));
        assertTrue(text.contains("TOTAL: 108.00"));
    }

    @Test
    void sectionsWithoutDataAreOmitted() {
        Invoice onlyInternal = ctx.invoices().userInvoice(email(2));
        assertTrue(onlyInternal.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), onlyInternal.getTotal());
        ok(ctx.tickets().createTicket("T002", email(3), "2026-10-15"));
        assertEquals(List.of("Individual tickets"), titles(ctx.invoices().userInvoice(email(3))));
    }

    @Test
    void reportsUnknownRecipientsAndIsNotPersisted() throws IOException {
        error(ctx.run("INVOICE CREATE;USER;nadie@correo.com"));
        error(ctx.run("INVOICE CREATE;GROUP;G99"));
        error(ctx.run("INVOICE CREATE;COMPANY;X"));
        try (var files = Files.list(dir)) {
            assertFalse(files.anyMatch(f -> f.getFileName().toString().contains("invoice")));
        }
    }

    @Test
    void directorAssemblesTheInvoiceInSteps() {
        List<String> steps = new ArrayList<>();
        InvoiceBuilder recording = new InvoiceBuilder() {
            private final StandardInvoiceBuilder delegate = new StandardInvoiceBuilder();

            @Override
            public void reset() {
                steps.add("reset");
                delegate.reset();
            }

            @Override
            public void buildHeader(String recipientType, String recipient, LocalDate date) {
                steps.add("header");
                delegate.buildHeader(recipientType, recipient, date);
            }

            @Override
            public void buildIndividualTickets(List<IndividualTicket> tickets) {
                steps.add("tickets");
                delegate.buildIndividualTickets(tickets);
            }

            @Override
            public void buildPasses(List<Pass> passes) {
                steps.add("passes");
                delegate.buildPasses(passes);
            }

            @Override
            public void buildGroupTickets(List<GroupTicket> groupTickets) {
                steps.add("groupTickets");
                delegate.buildGroupTickets(groupTickets);
            }

            @Override
            public Invoice getResult() {
                steps.add("result");
                return delegate.getResult();
            }
        };
        User user = ctx.users().findByEmail(email(1));
        new InvoiceDirector().buildUserInvoice(recording, user, TestContext.START, List.of(), List.of());
        assertEquals(List.of("reset", "header", "tickets", "passes", "result"), steps);
        steps.clear();
        new InvoiceDirector().buildGroupInvoice(recording, ctx.groups().find("G01"), TestContext.START, List.of());
        assertEquals(List.of("reset", "header", "groupTickets", "result"), steps);
    }
}
