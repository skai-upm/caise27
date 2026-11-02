package com.amusementpark.presentation;

import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndividualTicketTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) f.park.getTicketManager().find(id);
    }

    @Test
    void createsTicketWithCurrentPrice() {
        assertEquals("OK TICKET CREATED -> 40.00", f.run("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        IndividualTicket t = ticket("T001");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertSame(f.park.getUserManager().findByEmail("ana@correo.com"), t.getUser());
        assertEquals("40.00", t.getPrice().toPlainString());
    }

    @Test
    void ticketForTodayIsAllowed() {
        assertEquals("OK TICKET CREATED -> 40.00", f.run("TICKET CREATE;T001;ana@correo.com;2026-10-01"));
    }

    @Test
    void rejectsInvalidPurchases() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(f.run("TICKET CREATE;T002;nadie@correo.com;2026-10-15").startsWith("ERROR:"));
        assertTrue(f.run("TICKET CREATE;T001;ana@correo.com;2026-10-16").startsWith("ERROR:"));
        assertTrue(f.run("TICKET CREATE;T003;ana@correo.com;2026-09-30").startsWith("ERROR:"));
        assertTrue(f.run("TICKET CREATE;T004;ana@correo.com;2026-10-32").startsWith("ERROR:"));
        assertTrue(f.run("TICKET CREATE;;ana@correo.com;2026-10-15").startsWith("ERROR:"));
        assertEquals(1, f.park.getTicketManager().getTickets().size());
    }

    @Test
    void priceChangeIsNotRetroactive() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15", "SYSTEM PRICE;60.00");
        assertEquals("OK TICKET CREATED -> 60.00", f.run("TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertEquals("40.00", ticket("T001").getPrice().toPlainString());
    }

    @Test
    void deletesOnlyStrictlyFutureTickets() {
        f.ok("TICKET CREATE;TPAST;ana@correo.com;2026-10-14", "TICKET CREATE;TTODAY;ana@correo.com;2026-10-15",
                "TICKET CREATE;TFUT;ana@correo.com;2026-10-16", "SYSTEM DATE;2026-10-15");
        assertTrue(f.run("TICKET DELETE;TPAST").startsWith("ERROR:"));
        assertTrue(f.run("TICKET DELETE;TTODAY").startsWith("ERROR:"));
        assertEquals("OK: Ticket deleted: TFUT", f.run("TICKET DELETE;TFUT"));
        assertTrue(f.run("TICKET DELETE;NOPE").startsWith("ERROR:"));
        assertNotNull(ticket("TPAST"));
        assertNotNull(ticket("TTODAY"));
        assertNull(ticket("TFUT"));
    }

    @Test
    void currentOrFutureTicketBlocksUserDeletion() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(f.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        f.ok("SYSTEM DATE;2026-10-15");
        assertTrue(f.run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        assertNotNull(f.park.getUserManager().findByEmail("ana@correo.com"));
        assertNotNull(ticket("T001"));

        f.ok("SYSTEM DATE;2026-10-16", "USER DELETE;ana@correo.com;Clave1!");
        assertNull(f.park.getUserManager().findByEmail("ana@correo.com"));
        assertNotNull(ticket("T001"));
    }

    @Test
    void wrongCredentialsStillReportedFirst() {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("ERROR: Unknown email or wrong password.", f.run("USER DELETE;ana@correo.com;Mala1!"));
    }

    @Test
    void ticketsAndHistorySurviveRestart() throws IOException {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15", "TICKET CREATE;T002;ana@correo.com;2026-10-20");
        f.restart();
        assertEquals(2, f.park.getTicketManager().getTickets().size());
        assertSame(f.park.getUserManager().findByEmail("ana@correo.com"), ticket("T001").getUser());
        assertEquals("40.00", ticket("T002").getPrice().toPlainString());

        f.ok("SYSTEM DATE;2026-10-21", "USER DELETE;ana@correo.com;Clave1!");
        f.restart();
        User holder = ticket("T001").getUser();
        assertEquals("12345678Z", holder.getDni());
        assertEquals("ana@correo.com", holder.getEmail());
        assertEquals("Ana", holder.getName());
        assertEquals("Apellido", holder.getSurname());
        assertSame(holder, ticket("T002").getUser());
        assertTrue(f.park.getUserManager().getUsers().isEmpty());
    }

    @Test
    void snapshotFollowsEmailUpdates() throws IOException {
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-01", "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana2@correo.com");
        assertTrue(Files.readString(dir.resolve("tickets.txt")).contains("ana2@correo.com"));
    }
}
