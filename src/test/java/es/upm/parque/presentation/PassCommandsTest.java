package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.business.ParkController;
import es.upm.parque.business.model.Pass;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.UserRepository;

class PassCommandsTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = TestSupport.processor(new UserRepository());
        run("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "USER CREATE;Luis;Martin;11111111H;luis@correo.com;Clave3$;600000002;1234567890123458",
                "SYSTEM DATE;2027-04-01",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T002;pedro@correo.com;2027-04-15",
                "TICKET CREATE;TOD;ana@correo.com;2027-04-01",
                "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20");
    }

    private void run(String... lines) {
        for (String line : lines) {
            processor.process(line);
        }
    }

    private static PassRepository passes() {
        return ParkController.getInstance().getPasses();
    }

    @Test
    void singleSpeedPassCostsTwentyPercentOfTheTicket() {
        String r = processor.process("SPEEDPASS CREATE;SP01;T001;1");
        assertEquals("OK: 1 SpeedPass created for ticket T001 (SP01:1), unit price 8.00 EUR, total 8.00 EUR", r);
        assertNotNull(passes().findById("SP01:1"));
        assertNull(passes().findById("SP01"));
        assertFalse(passes().findById("SP01:1").isConsumed());
    }

    @Test
    void packageOfTenCreatesIndependentPassesWithoutDiscount() {
        String r = processor.process("SPEEDPASS CREATE;SP02;T001;10");
        assertEquals("OK: 10 SpeedPasses created for ticket T001 (SP02:1 to SP02:10), unit price 8.00 EUR,"
                + " total 80.00 EUR", r);
        for (int n = 1; n <= 10; n++) {
            assertEquals(new BigDecimal("8.00"), passes().findById("SP02:" + n).getPrice());
        }
        assertEquals(10, passes().findAll().size());
    }

    @Test
    void pricesUseTheStoredTicketPrice() {
        processor.process("SYSTEM PRICE;60");
        assertTrue(processor.process("SPECIALPASS CREATE;PE01;T001").endsWith("price 40.00 EUR"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP09;TG01:12345678Z;1").contains("unit price 7.20 EUR"));
        assertTrue(processor.process("SPECIALPASS CREATE;PE09;TG01:12345678Z").endsWith("price 36.00 EUR"));
    }

    @Test
    void invalidQuantityOrTicketCreatesNothing() {
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T001;5").startsWith("ERROR: invalid quantity"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T001;01").startsWith("ERROR"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T999;1").startsWith("ERROR: no individual ticket"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;TG01;1").startsWith("ERROR: no individual ticket"));
        assertTrue(processor.process("SPEEDPASS CREATE;;T001;1").startsWith("ERROR"));
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T001").startsWith("ERROR: SPEEDPASS CREATE expects"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T001;1").startsWith("ERROR"), "ticket in the past");
        assertTrue(processor.process("SPECIALPASS CREATE;PE03;T001").startsWith("ERROR"), "ticket in the past");
        assertTrue(passes().findAll().isEmpty());
    }

    @Test
    void ticketForTodayAcceptsPurchases() {
        assertTrue(processor.process("SPEEDPASS CREATE;SP04;TOD;1").startsWith("OK"));
        assertTrue(processor.process("SPECIALPASS CREATE;PE04;TOD").startsWith("OK"));
    }

    @Test
    void identifierConflictPreventsTheWholePackage() {
        processor.process("SPEEDPASS CREATE;SP02:7;T002;1");
        processor.process("SPEEDPASS CREATE;SP02;T001;1");
        assertEquals(2, passes().findAll().size());
        String r = processor.process("SPEEDPASS CREATE;SP02;T002;10");
        assertTrue(r.startsWith("ERROR") && r.contains("SP02:1"), r);
        assertEquals(2, passes().findAll().size());
        processor.process("SPECIALPASS CREATE;X:1;T002");
        assertTrue(processor.process("SPEEDPASS CREATE;X;T001;10").contains("X:1 is already in use"));
        assertEquals(3, passes().findAll().size());
    }

    @Test
    void passAndTicketIdentifierSpacesAreIndependent() {
        assertTrue(processor.process("SPECIALPASS CREATE;T001;T001").startsWith("OK"));
        assertTrue(processor.process("SPECIALPASS CREATE;T001;T002").startsWith("ERROR: a pass with identifier"));
        assertTrue(processor.process("SPECIALPASS CREATE;PE01;TG01:12345678Z").startsWith("OK"));
    }

    @Test
    void secondSpecialPassIsRejectedEvenWhenTheFirstWasConsumed() {
        assertTrue(processor.process("SPECIALPASS CREATE;PE01;T001").startsWith("OK"));
        assertTrue(processor.process("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        passes().findById("PE01").consume();
        String r = processor.process("SPECIALPASS CREATE;PE02;T001");
        assertTrue(r.startsWith("ERROR") && r.contains("consumed"), r);
        assertNull(passes().findById("PE02"));
        assertTrue(processor.process("SPECIALPASS DELETE;PE01").startsWith("ERROR"), "consumed pass kept");
        assertNotNull(passes().findById("PE01"));
    }

    @Test
    void refundedSpecialPassCanBeBoughtAgain() {
        processor.process("SPECIALPASS CREATE;PE01;T001");
        assertEquals("OK: Special Pass PE01 refunded, 40.00 EUR", processor.process("SPECIALPASS DELETE;PE01"));
        assertTrue(processor.process("SPECIALPASS CREATE;PE02;T001").startsWith("OK"));
    }

    @Test
    void packagePassesAreRefundedSeparately() {
        processor.process("SPEEDPASS CREATE;SP02;T001;10");
        assertEquals("OK: SpeedPass SP02:3 refunded, 8.00 EUR", processor.process("SPEEDPASS DELETE;SP02:3"));
        assertNull(passes().findById("SP02:3"));
        assertEquals(9, passes().findAll().size());
        assertTrue(processor.process("SPEEDPASS DELETE;SP02:3").startsWith("ERROR"));
    }

    @Test
    void refundsAreRejectedForConsumedTodayOrWrongType() {
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("SPEEDPASS CREATE;SP04;TOD;1");
        processor.process("SPECIALPASS CREATE;PE01;T002");
        passes().findById("SP01:1").consume();
        assertTrue(processor.process("SPEEDPASS DELETE;SP01:1").contains("consumed"));
        assertTrue(processor.process("SPEEDPASS DELETE;SP04:1").contains("cannot be refunded"));
        assertTrue(processor.process("SPECIALPASS DELETE;SP04:1").startsWith("ERROR: no Special Pass"));
        assertTrue(processor.process("SPEEDPASS DELETE;PE01").startsWith("ERROR: no SpeedPass"));
        assertTrue(processor.process("SPEEDPASS DELETE;NOPE").startsWith("ERROR"));
        assertTrue(processor.process("SPECIALPASS DELETE;PE01;x").startsWith("ERROR"));
        assertEquals(3, passes().findAll().size());
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("SPECIALPASS DELETE;PE01").startsWith("ERROR"), "ticket valid today");
    }

    @Test
    void ticketDeletionRemovesAllItsPasses() {
        run("SPEEDPASS CREATE;SP01;T001;1", "SPEEDPASS CREATE;SP02;T001;10", "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SP05;T002;1");
        assertEquals("OK: ticket T001 deleted and 12 passes", processor.process("TICKET DELETE;T001"));
        assertEquals(1, passes().findAll().size());
        assertNotNull(passes().findById("SP05:1"));
        assertEquals("OK: ticket T002 deleted and 1 pass", processor.process("TICKET DELETE;T002"));
    }

    @Test
    void groupTicketDeletionRemovesPassesThenCompositionThenTicket() {
        run("SPEEDPASS CREATE;SP05;TG01:12345678Z;1", "SPECIALPASS CREATE;PE05;TG01:87654321X",
                "SPEEDPASS CREATE;SP06;T001;1");
        assertEquals("OK: group ticket TG01 deleted with its 3 tickets and 2 passes",
                processor.process("GROUPTICKET DELETE;TG01"));
        assertEquals(1, passes().findAll().size());
        assertNull(ParkController.getInstance().getTickets().findById("TG01:12345678Z"));
    }

    @Test
    void rejectedDeletionsKeepThePasses() {
        run("SPEEDPASS CREATE;SP05;TG01:12345678Z;1", "SPEEDPASS CREATE;SP06;T001;1");
        assertTrue(processor.process("TICKET DELETE;TG01:12345678Z").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-20");
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertEquals(2, passes().findAll().size());
    }

    @Test
    void userInvoiceListsPassesIncludingThoseOfGroupComponents() {
        run("SPEEDPASS CREATE;SP01;T001;1", "SPECIALPASS CREATE;PE01;TG01:12345678Z",
                "SPEEDPASS CREATE;SP07;T002;1");
        passes().findById("SP01:1").consume();
        String r = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(r.contains("SpeedPasses:" + System.lineSeparator()
                + "    SpeedPass SP01:1 for ticket T001 (consumed)  8.00 EUR"), r);
        assertTrue(r.contains("Special Passes:" + System.lineSeparator()
                + "    Special Pass PE01 for ticket TG01:12345678Z  36.00 EUR"), r);
        assertFalse(r.contains("SP07"), r);
        assertTrue(r.endsWith("TOTAL: 124.00 EUR"), r);
        String group = processor.process("INVOICE CREATE;GROUP;G01");
        assertFalse(group.contains("PE01"), group);
        assertTrue(group.endsWith("TOTAL: 108.00 EUR"), group);
    }

    @Test
    void passKnowsItsTicket() {
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        Pass pass = passes().findById("SP01:1");
        assertEquals("T001", pass.getTicket().getId());
        assertEquals(1, passes().passesOf(pass.getTicket()).size());
        assertNull(passes().specialPassOf(pass.getTicket()));
    }
}
