package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, TODAY);
        t.run(ParkTestSupport.CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2026-10-15");
    }

    private IndividualTicket t001() {
        return t.tickets.getIndividual("T001");
    }

    @Test
    void buySingleSpeedPassAtTwentyPercent() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [T001-SP1]", t.run("SPEEDPASS CREATE;T001;1"));
        SpeedPass pass = t001().getSpeedPasses().get(0);
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
        assertFalse(pass.isConsumed());
    }

    @Test
    void buyPackOfTenIndependentPasses() {
        t.run("SPEEDPASS CREATE;T001;1");
        String out = t.run("SPEEDPASS CREATE;T001;10");
        assertTrue(out.startsWith("OK SPEEDPASS CREATED -> 80.00 [T001-SP2, T001-SP3"), out);
        assertTrue(out.endsWith("T001-SP11]"), out);
        assertEquals(11, t001().getSpeedPasses().size());
        assertEquals(11, t001().getSpeedPasses().stream().map(SpeedPass::getId).distinct().count());
    }

    @Test
    void speedPassPriceFollowsTheTicketPrice() {
        t.run("SYSTEM PRICE;55.00", "TICKET CREATE;T002;ana@correo.com;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 11.00 [T002-SP1]", t.run("SPEEDPASS CREATE;T002;1"));
    }

    @Test
    void invalidSpeedPassPurchases() {
        t.run("GROUP CREATE;G01;Familia");
        t.addMembers("G01", 1);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "TICKET CREATE;T009;ana@correo.com;2026-10-02",
                "SYSTEM DATE;2026-10-03");
        assertTrue(t.run("SPEEDPASS CREATE;T001;2").startsWith("ERROR"));
        assertTrue(t.run("SPEEDPASS CREATE;T001;0").startsWith("ERROR"));
        assertTrue(t.run("SPEEDPASS CREATE;TG01;1").startsWith("ERROR"));
        assertTrue(t.run("SPEEDPASS CREATE;NOPE;1").startsWith("ERROR"));
        assertTrue(t.run("SPEEDPASS CREATE;T009;1").startsWith("ERROR"));
        assertTrue(t001().getSpeedPasses().isEmpty());
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [TG01-1-SP1]", t.run("SPEEDPASS CREATE;TG01-1;1"));
    }

    @Test
    void specialPassAtTicketPriceOnlyOnce() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00", t.run("SPECIALPASS CREATE;T001"));
        SpecialPass pass = t001().getSpecialPass();
        assertEquals("T001-SPECIAL", pass.getId());
        assertTrue(t.run("SPECIALPASS CREATE;T001").startsWith("ERROR"));
        pass.consume();
        assertTrue(t.run("SPECIALPASS CREATE;T001").startsWith("ERROR"));
    }

    @Test
    void refundUnusedPassesBeforeTheTicketDate() {
        t.run("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001");
        assertEquals("OK SPEEDPASS DELETED -> 8.00", t.run("SPEEDPASS DELETE;T001-SP1"));
        assertEquals(9, t001().getSpeedPasses().size());
        assertEquals("OK SPECIALPASS DELETED -> 40.00", t.run("SPECIALPASS DELETE;T001"));
        assertNull(t001().getSpecialPass());
        assertEquals("OK SPECIALPASS CREATED -> 40.00", t.run("SPECIALPASS CREATE;T001"));
        assertTrue(t.run("SPEEDPASS DELETE;T001-SP1").startsWith("ERROR"));
        assertTrue(t.run("SPECIALPASS DELETE;T999").startsWith("ERROR"));
    }

    @Test
    void refundNotPermittedWhenConsumedOrOnTheTicketDate() {
        t.run("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001");
        t001().getSpeedPasses().get(0).consume();
        assertTrue(t.run("SPEEDPASS DELETE;T001-SP1").contains("already used"));
        t.run("SYSTEM DATE;2026-10-15");
        assertTrue(t.run("SPEEDPASS DELETE;T001-SP2").startsWith("ERROR"));
        assertTrue(t.run("SPECIALPASS DELETE;T001").startsWith("ERROR"));
        assertEquals(10, t001().getSpeedPasses().size());
        t.run("SYSTEM DATE;2026-10-14");
        t001().getSpecialPass().consume();
        assertTrue(t.run("SPECIALPASS DELETE;T001").contains("already used"));
        t.run("TICKET CREATE;T002;ana@correo.com;2026-10-20");
        assertTrue(t.run("SPECIALPASS DELETE;T002").startsWith("ERROR"));
    }

    @Test
    void passesArePersistedWithPriceTicketAndStatus() throws IOException {
        t.run("SPEEDPASS CREATE;T001;1", "SPECIALPASS CREATE;T001", "SYSTEM DATE;2026-10-15",
                "ATTRACTION CREATE;A01;Tornado;10", "ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals("T001-SP1;SPEED;T001;8.00;true" + System.lineSeparator()
                + "T001-SPECIAL;SPECIAL;T001;40.00;false" + System.lineSeparator(),
                Files.readString(dir.resolve("passes.txt")));
        IndividualTicket restored = t.restart(TODAY).tickets.getIndividual("T001");
        assertTrue(restored.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), restored.getSpeedPasses().get(0).getPrice());
        assertFalse(restored.getSpecialPass().isConsumed());
        assertEquals(restored, restored.getSpecialPass().getTicket());
    }

    @Test
    void passesOfUnknownTicketsAreSkipped() throws IOException {
        Files.writeString(dir.resolve("passes.txt"), "X-SP1;SPEED;NOPE;8.00;false\n");
        assertTrue(t.restart(TODAY).tickets.getIndividual("T001").getPasses().isEmpty());
    }
}
