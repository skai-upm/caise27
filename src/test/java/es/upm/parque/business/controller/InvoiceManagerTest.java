package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.Park;

class InvoiceManagerTest {

    private static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private Park park;

    @BeforeEach
    void setUp() {
        park = new Park(dir, LocalDate.of(2026, 10, 1));
        park.users.create(Fixtures.ana());
        park.users.create(Fixtures.pedro());
        park.users.create(Fixtures.numbered(1));
        park.groups.create("G01", "Familia Garcia");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        park.groups.addMember("G01", "u1@correo.com");
    }

    @Test
    void userInvoiceCountsOnlyDirectTickets() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.setPrice("60");
        park.tickets.create("T002", "ana@correo.com", "2026-10-20");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.tickets.create("T003", "pedro@correo.com", "2026-10-15");
        assertEquals(String.join(NL, "INVOICE USER ana@correo.com - Ana Garcia Lopez", "Date: 2026-10-01",
                "Individual tickets:", "- T001 2026-10-15 40.00", "- T002 2026-10-20 60.00", "TOTAL: 100.00"),
                park.invoices.create("USER", "ana@correo.com"));
    }

    @Test
    void userWithoutPurchases() {
        assertEquals(String.join(NL, "INVOICE USER ana@correo.com - Ana Garcia Lopez", "Date: 2026-10-01",
                "No purchases", "TOTAL: 0.00"), park.invoices.create("user", "ana@correo.com"));
    }

    @Test
    void groupInvoiceListsGroupTicketsOnly() {
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(String.join(NL, "INVOICE GROUP G01 - Familia Garcia", "Date: 2026-10-01", "Group tickets:",
                "- TG01 2026-10-15 (3 members, 10% discount) 108.00", "TOTAL: 108.00"),
                park.invoices.create("GROUP", "G01"));
    }

    @Test
    void rejectsUnknownRecipients() {
        assertEquals(GroupManager.USER_NOT_FOUND, park.invoices.create("USER", "nadie@correo.com"));
        assertEquals(GroupManager.GROUP_NOT_FOUND, park.invoices.create("GROUP", "G99"));
        assertEquals(InvoiceManager.INVALID_TYPE, park.invoices.create("FAMILY", "G01"));
    }

    @Test
    void ticketQueriesSeparateDirectAndGroupTickets() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(1, park.tickets.ticketsHeldBy(park.users.findByEmail("ana@correo.com")).size());
        assertEquals(0, park.tickets.ticketsHeldBy(park.users.findByEmail("pedro@correo.com")).size());
        assertEquals(1, park.tickets.groupTicketsOf(park.groups.find("G01")).size());
    }

    @Test
    void userInvoiceIncludesPassesOfDirectAndGroupTickets() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.passes.createSpeedPasses("T001", "1");
        park.passes.createSpecialPass("TG01-1");
        assertEquals(String.join(NL, "INVOICE USER ana@correo.com - Ana Garcia Lopez", "Date: 2026-10-01",
                "Individual tickets:", "- T001 2026-10-15 40.00", "Personal passes:", "- T001-S1 SPEED 8.00",
                "- TG01-1-SPECIAL SPECIAL 40.00", "TOTAL: 88.00"), park.invoices.create("USER", "ana@correo.com"));
        assertEquals(String.join(NL, "INVOICE GROUP G01 - Familia Garcia", "Date: 2026-10-01", "Group tickets:",
                "- TG01 2026-10-15 (3 members, 10% discount) 108.00", "TOTAL: 108.00"),
                park.invoices.create("GROUP", "G01"));
    }
}
