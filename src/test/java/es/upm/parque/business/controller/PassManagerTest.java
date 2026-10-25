package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.Park;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.persistence.TextFilePassRepository;

class PassManagerTest {

    @TempDir
    Path dir;

    private Park park;
    private PassManager passes;

    @BeforeEach
    void setUp() {
        park = new Park(dir, LocalDate.of(2026, 10, 1));
        passes = park.passes;
        park.users.create(Fixtures.ana());
        park.users.create(Fixtures.pedro());
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.groups.create("G01", "Familia");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
    }

    private IndividualTicket t001() {
        return park.tickets.findIndividual("T001");
    }

    @Test
    void buysSingleAndPacksOfSpeedPasses() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 (T001-S1)", passes.createSpeedPasses("T001", "1"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00 (T001-S2..T001-S11)", passes.createSpeedPasses("T001", "10"));
        assertEquals(11, t001().getSpeedPasses().size());
        assertTrue(t001().getSpeedPasses().stream().allMatch(p -> p.getPrice().compareTo(new BigDecimal("8")) == 0));
        assertEquals(PassManager.INVALID_QUANTITY, passes.createSpeedPasses("T001", "5"));
        assertEquals(PassManager.INVALID_QUANTITY, passes.createSpeedPasses("T001", "uno"));
    }

    @Test
    void passesOnGroupGeneratedTicketsButNotOnGroupTickets() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 (TG01-1-S1)", passes.createSpeedPasses("TG01-1", "1"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, passes.createSpeedPasses("TG01", "1"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, passes.createSpecialPass("T999"));
    }

    @Test
    void atMostOneSpecialPassPerTicket() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00 (T001-SPECIAL)", passes.createSpecialPass("T001"));
        assertEquals(PassManager.SPECIAL_ALREADY_ISSUED, passes.createSpecialPass("T001"));
        passes.consume(t001().getSpecialPass());
        assertEquals(PassManager.SPECIAL_ALREADY_ISSUED, passes.createSpecialPass("T001"));
        assertTrue(t001().getSpecialPass().isConsumed());
    }

    @Test
    void expiredTicketsCannotGetPasses() {
        park.date.set(LocalDate.of(2026, 10, 16));
        assertEquals(PassManager.TICKET_EXPIRED, passes.createSpeedPasses("T001", "1"));
        assertEquals(PassManager.TICKET_EXPIRED, passes.createSpecialPass("T001"));
    }

    @Test
    void refundsOnlyUnusedPassesBeforeTheTicketDate() {
        passes.createSpeedPasses("T001", "10");
        passes.createSpecialPass("T001");
        passes.consume(t001().getSpeedPasses().get(1));
        assertEquals("OK SPEEDPASS DELETED -> 8.00", passes.deleteSpeedPass("T001-S1"));
        assertEquals(PassManager.PASS_CONSUMED, passes.deleteSpeedPass("T001-S2"));
        assertEquals(PassManager.PASS_NOT_FOUND, passes.deleteSpeedPass("T001-S1"));
        assertEquals(9, t001().getSpeedPasses().size());
        park.date.set(LocalDate.of(2026, 10, 15));
        assertEquals(TicketManager.NOT_REFUNDABLE, passes.deleteSpeedPass("T001-S3"));
        assertEquals(TicketManager.NOT_REFUNDABLE, passes.deleteSpecialPass("T001"));
        park.date.set(LocalDate.of(2026, 10, 1));
        assertEquals("OK SPECIALPASS DELETED -> 40.00", passes.deleteSpecialPass("T001"));
        assertNull(t001().getSpecialPass());
        assertEquals(PassManager.PASS_NOT_FOUND, passes.deleteSpecialPass("T001"));
        assertEquals(PassManager.PASS_NOT_FOUND, passes.deleteSpecialPass("T999"));
        assertEquals("OK SPECIALPASS CREATED -> 40.00 (T001-SPECIAL)", passes.createSpecialPass("T001"));
    }

    @Test
    void consumedSpecialPassCannotBeRefunded() {
        passes.createSpecialPass("T001");
        passes.consume(t001().getSpecialPass());
        assertEquals(PassManager.PASS_CONSUMED, passes.deleteSpecialPass("T001"));
    }

    @Test
    void passesSurviveRestart() {
        passes.createSpeedPasses("T001", "1");
        passes.createSpecialPass("TG01-2");
        passes.consume(t001().getSpeedPasses().get(0));
        Park restarted = new Park(dir, LocalDate.of(2026, 10, 1));
        SpeedPass restoredSpeed = restarted.tickets.findIndividual("T001").getSpeedPasses().get(0);
        assertTrue(restoredSpeed.isConsumed());
        assertEquals(new BigDecimal("8.00"), restoredSpeed.getPrice());
        assertNotNull(restarted.tickets.findAnyIndividual("TG01-2").getSpecialPass());
        assertEquals("OK SPEEDPASS CREATED -> 8.00 (T001-S2)", restarted.passes.createSpeedPasses("T001", "1"));
    }

    @Test
    void ticketLookupsCoverGroupGeneratedTickets() {
        assertEquals(3, park.tickets.allIndividualTickets().size());
        assertEquals(2, park.tickets.individualTicketsOf(park.users.findByEmail("ana@correo.com")).size());
        assertNull(park.tickets.findAnyIndividual("TG01"));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = Files.createFile(dir.resolve("blocked"));
        PassManager broken = PassManager.initialize(new TextFilePassRepository(blocked.resolve("p.txt")),
                park.tickets, park.date);
        assertEquals(UserManager.STORAGE_FAILURE, broken.createSpecialPass("T001"));
    }
}
