package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.model.Pass;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassCommandsTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        p.process(TestData.CREATE_PEDRO);
        p.process(TestData.CREATE_LUIS);
        p.process("SYSTEM DATE;2026-10-01");
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
    }

    private List<Pass> passes() {
        return PassController.getInstance().getPasses();
    }

    @Test
    void singleSpeedPassAndPackOfTen() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00", p.process("SPEEDPASS CREATE;S1;T001;1"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00", p.process("SPEEDPASS CREATE;S2;T001;10"));
        assertEquals(11, passes().size());
        assertTrue(passes().stream().allMatch(x -> x.getPrice().toPlainString().equals("8.00") && !x.isConsumed()));
        assertTrue(PassController.getInstance().find("S2-10").isPresent());
        assertTrue(PassController.getInstance().find("S1-1").isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SPEEDPASS CREATE;S3;T001;2", "SPEEDPASS CREATE;S3;T001;0", "SPEEDPASS CREATE;S3;T001;abc",
        "SPEEDPASS CREATE;S3;T001;-10", "SPEEDPASS CREATE;S3;T999;1", "SPEEDPASS CREATE;S3;T001", "SPEEDPASS CREATE;;T001;1",
        "SPECIALPASS CREATE;SP9;T999", "SPECIALPASS CREATE;SP9", "SPEEDPASS DELETE", "SPECIALPASS DELETE;NOPE"})
    void rejectedPurchasesCreateNothing(String command) {
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertTrue(passes().isEmpty());
    }

    @Test
    void identifierConflictCreatesNoPartialPack() {
        p.process("SPECIALPASS CREATE;S4-5;T001");
        assertTrue(p.process("SPEEDPASS CREATE;S4;T001;10").startsWith("ERROR:"));
        assertEquals(1, passes().size());
        assertTrue(p.process("SPECIALPASS CREATE;S4-5;T001").startsWith("ERROR:"));
    }

    @Test
    void onlyOneSpecialPassPerTicketEvenWhenConsumed() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00", p.process("SPECIALPASS CREATE;SP1;T001"));
        assertTrue(p.process("SPECIALPASS CREATE;SP2;T001").startsWith("ERROR:"));
        PassController.getInstance().find("SP1").orElseThrow().markConsumed();
        assertTrue(p.process("SPECIALPASS CREATE;SP2;T001").startsWith("ERROR:"));
        assertTrue(p.process("SPECIALPASS DELETE;SP1").startsWith("ERROR:")); // consumed: not refundable
        assertTrue(PassController.getInstance().find("SP1").isPresent());     // and still recorded
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() {
        p.process("SPEEDPASS CREATE;S1;T001;10");
        p.process("SPECIALPASS CREATE;SP1;T001");
        assertEquals("OK: Pass S1-1 refunded (8.00)", p.process("SPEEDPASS DELETE;S1-1"));
        assertTrue(p.process("SPECIALPASS DELETE;S1-2").startsWith("ERROR:"));  // wrong type
        assertTrue(p.process("SPEEDPASS DELETE;SP1").startsWith("ERROR:"));     // wrong type
        PassController.getInstance().find("S1-3").orElseThrow().markConsumed();
        assertTrue(p.process("SPEEDPASS DELETE;S1-3").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("SPEEDPASS DELETE;S1-2").startsWith("ERROR:"));    // ticket is today
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("SPECIALPASS DELETE;SP1").startsWith("ERROR:"));   // historical
        p.process("SYSTEM DATE;2026-10-14");
        assertEquals("OK: Pass SP1 refunded (40.00)", p.process("SPECIALPASS DELETE;SP1"));
        assertEquals("OK SPECIALPASS CREATED -> 40.00", p.process("SPECIALPASS CREATE;SP2;T001"));
    }

    @Test
    void pastTicketCannotGetPasses() {
        p.process("TICKET CREATE;T002;ana@correo.com;2026-10-01");
        p.process("SYSTEM DATE;2026-10-02");
        assertTrue(p.process("SPEEDPASS CREATE;S1;T002;1").startsWith("ERROR:"));
        assertTrue(p.process("SPECIALPASS CREATE;SP1;T002").startsWith("ERROR:"));
    }

    @Test
    void passesOnDiscountedConstituentTicketsAndInvoices() {
        p.process("GROUP CREATE;G01;Familia");
        p.process("GROUP ADD;G01;ana@correo.com");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("GROUP ADD;G01;luis@correo.com");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals("OK SPEEDPASS CREATED -> 7.20", p.process("SPEEDPASS CREATE;S5;TG01-1;1"));
        assertEquals("OK SPECIALPASS CREATED -> 36.00", p.process("SPECIALPASS CREATE;SP5;TG01-1"));
        List<String> ana = p.process("INVOICE CREATE;USER;ana@correo.com").lines().toList();
        assertTrue(ana.contains("Personal passes:"));
        assertTrue(ana.contains("  SP5 SPECIAL (ticket TG01-1) 36.00"));
        assertTrue(ana.contains("  T001 2026-10-15 40.00"));
        assertFalse(ana.stream().anyMatch(l -> l.contains("TG01-1 2026")));
        assertEquals("Total: 83.20", ana.get(ana.size() - 1));
        List<String> group = p.process("INVOICE CREATE;GROUP;G01").lines().toList();
        assertEquals("Total: 108.00", group.get(group.size() - 1));
        assertFalse(group.contains("Personal passes:"));
    }

    @Test
    void laterPriceChangesDoNotAffectPasses() {
        p.process("SPEEDPASS CREATE;S1;T001;1");
        p.process("SYSTEM PRICE;60.00");
        assertEquals("8.00", PassController.getInstance().find("S1-1").orElseThrow().getPrice().toPlainString());
    }

    @Test
    void passesAreRestoredAfterRestart() throws IOException {
        p.process("SPEEDPASS CREATE;S1;T001;1");
        p.process("SPECIALPASS CREATE;SP1;T001");
        assertEquals(List.of("S1-1;SPEED;T001;8.00;false", "SP1;SPECIAL;T001;40.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));
        CommandProcessor restarted = App.createProcessor(dir);
        restarted.process("SYSTEM DATE;2026-10-01");
        assertEquals(2, passes().size());
        assertTrue(restarted.process("SPECIALPASS CREATE;SP2;T001").startsWith("ERROR:"));
        assertTrue(restarted.process("SPEEDPASS CREATE;S1;T001;1").startsWith("ERROR:"));
        assertEquals("OK: Pass S1-1 refunded (8.00)", restarted.process("SPEEDPASS DELETE;S1-1"));
        assertEquals(List.of("SP1;SPECIAL;T001;40.00;false"), Files.readAllLines(dir.resolve("passes.txt")));
    }
}
