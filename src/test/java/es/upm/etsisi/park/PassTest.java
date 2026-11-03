package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, ParkFixture.LUIS,
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "TICKET CREATE;T002;ana@correo.com;2027-04-20");
    }

    private Pass pass(String id) {
        return f.park.findPass(id);
    }

    @Test
    void singleSpeedPass() {
        assertEquals("SPEEDPASS CREATED: SP01:1. TOTAL PRICE: 8.00 EUR", f.run("SPEEDPASS CREATE;SP01;T001;1"));
        Pass p = pass("SP01:1");
        assertTrue(p instanceof SpeedPass);
        assertEquals("T001", p.getTicket().getId());
        assertFalse(p.isConsumed());
    }

    @Test
    void tenPackHasIndependentIdsAndNoDiscount() {
        assertTrue(f.run("SPEEDPASS CREATE;SP02;T001;10").endsWith("TOTAL PRICE: 80.00 EUR"));
        for (int i = 1; i <= 10; i++) {
            assertEquals("8.00", pass("SP02:" + i).getPrice().toPlainString());
        }
        assertNull(pass("SP02:11"));
    }

    @Test
    void speedPassRejections() {
        assertTrue(f.run("SPEEDPASS CREATE;SP03;T001;5").contains("invalid quantity"));
        assertTrue(f.run("SPEEDPASS CREATE;SP03;T999;1").startsWith("ERROR: no ticket"));
        f.run("GROUP CREATE;G01;F", "GROUP ADD;G01;pedro@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(f.run("SPEEDPASS CREATE;SP03;TG01;1").contains("only be bought for individual tickets"));
        f.run("SYSTEM DATE;2027-04-16");
        assertTrue(f.run("SPEEDPASS CREATE;SP03;T001;1").contains("before the current system date"));
        assertTrue(f.park.getPasses().isEmpty());
    }

    @Test
    void identifierConflictCreatesNothing() {
        f.run("SPECIALPASS CREATE;SP04:2;T002");
        assertTrue(f.run("SPEEDPASS CREATE;SP04;T001;10").contains("SP04:2 is already in use"));
        assertNull(pass("SP04:1"));
        assertEquals(1, f.park.getPasses().size());
    }

    @Test
    void passPricesUseDiscountedComposedTicket() {
        f.run("GROUP CREATE;G01;Trio", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15", "SYSTEM PRICE;100");
        assertTrue(f.run("SPEEDPASS CREATE;SP05;TG01:12345678Z;1").endsWith("TOTAL PRICE: 7.20 EUR"));
        assertEquals("SPECIALPASS CREATED. PRICE: 36.00 EUR", f.run("SPECIALPASS CREATE;PE05;TG01:12345678Z"));
    }

    @Test
    void oneSpecialPassPerTicket() {
        assertEquals("SPECIALPASS CREATED. PRICE: 40.00 EUR", f.run("SPECIALPASS CREATE;PE01;T001"));
        assertTrue(f.run("SPECIALPASS CREATE;PE02;T001").contains("already has Special Pass PE01"));
        assertTrue(f.run("SPECIALPASS CREATE;PE01;T002").contains("already in use"));
        assertNull(pass("PE02"));
    }

    @Test
    void refunds() {
        f.run("SPEEDPASS CREATE;SP06;T002;10", "SPEEDPASS CREATE;SP07;T001;1", "SPECIALPASS CREATE;PE06;T002");
        assertEquals("SPEEDPASS DELETED", f.run("SPEEDPASS DELETE;SP06:3"));
        assertNull(pass("SP06:3"));
        assertNotNull(pass("SP06:4"));
        assertTrue(f.run("SPEEDPASS DELETE;SP07:1").contains("not dated after"));
        assertTrue(f.run("SPEEDPASS DELETE;PE06").startsWith("ERROR: no SpeedPass"));
        assertTrue(f.run("SPECIALPASS DELETE;SP06:1").startsWith("ERROR: no Special Pass"));
        assertTrue(f.run("SPEEDPASS DELETE;NOPE").startsWith("ERROR: no SpeedPass"));
        assertEquals("SPECIALPASS DELETED", f.run("SPECIALPASS DELETE;PE06"));
        assertEquals("SPECIALPASS CREATED. PRICE: 40.00 EUR", f.run("SPECIALPASS CREATE;PE07;T002"));
    }

    @Test
    void consumedPassCannotBeRefundedAndStaysStored() {
        f.run("ATTRACTION CREATE;A01;Dragon;10", "SPEEDPASS CREATE;SP08;T001;1", "ATTRACTION ENTER;A01;T001;SPEED",
                "SYSTEM DATE;2027-04-10");
        assertTrue(f.run("SPEEDPASS DELETE;SP08:1").contains("has been consumed"));
        assertTrue(pass("SP08:1").isConsumed());
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        f.run("SPEEDPASS CREATE;SP09;T002;1", "SPEEDPASS CREATE;SP10;T002;10", "SPECIALPASS CREATE;PE09;T002",
                "SPEEDPASS CREATE;SP11;T001;1");
        assertEquals("TICKET DELETED", f.run("TICKET DELETE;T002"));
        assertEquals(1, f.park.getPasses().size());
        assertNotNull(pass("SP11:1"));
    }

    @Test
    void groupTicketDeletionCascades() throws IOException {
        f.run("GROUP CREATE;G01;Pareja", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20", "SPEEDPASS CREATE;SP12;TG01:12345678Z;10",
                "SPECIALPASS CREATE;PE12;TG01:87654321X");
        assertEquals("GROUPTICKET DELETED", f.run("GROUPTICKET DELETE;TG01"));
        f.restart();
        assertTrue(f.park.getPasses().isEmpty());
        assertNull(f.park.findTicket("TG01"));
        assertNull(f.park.findTicket("TG01:12345678Z"));
    }

    @Test
    void failedSaveUndoesCascadeAndPurchases() throws IOException {
        f.run("GROUP CREATE;G01;Pareja", "GROUP ADD;G01;ana@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-20",
                "SPEEDPASS CREATE;SP13;TG01:12345678Z;1", "SPEEDPASS CREATE;SP14;T002;1");
        Park park = TestParks.start(new FailingRepository(dir));
        es.upm.etsisi.park.presentation.CommandProcessor p = TestParks.processor(new FailingRepository(dir),
                new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        p.process("GROUPTICKET DELETE;TG01");
        p.process("TICKET DELETE;T002");
        p.process("SPEEDPASS CREATE;SP15;T002;10");
        p.process("SPECIALPASS CREATE;PE15;T002");
        p.process("SPEEDPASS DELETE;SP14:1");
        assertNotNull(park.findTicket("TG01"));
        assertNotNull(park.findTicket("T002"));
        assertEquals(2, park.getPasses().size());
    }
}
