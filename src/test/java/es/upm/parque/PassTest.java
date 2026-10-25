package es.upm.parque;

import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassKind;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.persistence.TextPassRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PassTest extends ParkTestSupport {

    @BeforeEach
    void ticket() {
        createAna();
        createPedro();
        ok("SYSTEM DATE;2026-10-01");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
    }

    private List<Pass> passesOf(String ticketId) {
        return passes().passesOf(tickets().findIndividual(ticketId));
    }

    @Test
    public void buysOneSpeedPassAtTwentyPercent() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 (T001-SP1)", run("SPEEDPASS CREATE;T001;1"));
        SpeedPass pass = assertInstanceOf(SpeedPass.class, passes().find("T001-SP1"));
        assertEquals("8.00", pass.getPrice().toPlainString());
        assertFalse(pass.isConsumed());
        assertSame(tickets().findIndividual("T001"), pass.getTicket());
    }

    @Test
    public void buysPackOfTenIndependentPasses() {
        ok("SPEEDPASS CREATE;T001;1");
        String output = run("SPEEDPASS CREATE;T001;10");
        assertTrue(output.startsWith("OK SPEEDPASS CREATED -> 80.00 (T001-SP2, "), output);
        List<Pass> all = passesOf("T001");
        assertEquals(11, all.size());
        assertEquals(11, new HashSet<>(all.stream().map(Pass::getId).toList()).size());
        assertTrue(all.stream().allMatch(pass -> pass.getPrice().toPlainString().equals("8.00") && !pass.isConsumed()));
    }

    @Test
    public void speedPassPriceFollowsTheTicketPrice() {
        ok("SYSTEM PRICE;55.55");
        ok("TICKET CREATE;T002;" + PEDRO + ";2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 11.11 (T002-SP1)", run("SPEEDPASS CREATE;T002;1"));
        assertEquals("OK SPECIALPASS CREATED -> 55.55 (T002-SPECIAL)", run("SPECIALPASS CREATE;T002"));
    }

    @Test
    public void rejectsInvalidSpeedPassPurchases() {
        fails("SPEEDPASS CREATE;T001;2");
        fails("SPEEDPASS CREATE;T001;0");
        fails("SPEEDPASS CREATE;T999;1");
        ok("GROUP CREATE;G01;Grupo");
        ok("GROUP ADD;G01;" + PEDRO);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        fails("SPEEDPASS CREATE;TG01;1");
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SYSTEM DATE;2026-10-16");
        fails("SPEEDPASS CREATE;T001;1");
        fails("SPECIALPASS CREATE;T001");
        assertEquals(1, passes().getPasses().size());
    }

    @Test
    public void onlyOneSpecialPassPerTicket() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00 (T001-SPECIAL)", run("SPECIALPASS CREATE;T001"));
        fails("SPECIALPASS CREATE;T001");
        passes().findSpecialPass(tickets().findIndividual("T001")).consume();
        fails("SPECIALPASS CREATE;T001");
        assertEquals(1, passesOf("T001").size());
    }

    @Test
    public void refundsUnusedPassesOfFutureTickets() {
        ok("SPEEDPASS CREATE;T001;1");
        ok("SPECIALPASS CREATE;T001");
        fails("SPECIALPASS DELETE;T001-SP1");
        fails("SPEEDPASS DELETE;T001-SPECIAL");
        fails("SPEEDPASS DELETE;NOPE");
        assertEquals("OK SPEEDPASS DELETED -> 8.00", run("SPEEDPASS DELETE;T001-SP1"));
        assertEquals("OK SPECIALPASS DELETED -> 40.00", run("SPECIALPASS DELETE;T001-SPECIAL"));
        assertTrue(passesOf("T001").isEmpty());
        ok("SPECIALPASS CREATE;T001");
    }

    @Test
    public void doesNotRefundConsumedOrCurrentPasses() {
        ok("SPEEDPASS CREATE;T001;10");
        passes().find("T001-SP1").consume();
        fails("SPEEDPASS DELETE;T001-SP1");
        ok("SYSTEM DATE;2026-10-15");
        fails("SPEEDPASS DELETE;T001-SP2");
        assertNotNull(passes().find("T001-SP1"));
        assertNotNull(passes().find("T001-SP2"));
    }

    @Test
    public void passesSurviveRestartWithPriceTicketAndState() throws Exception {
        ok("SPEEDPASS CREATE;T001;1");
        ok("SPECIALPASS CREATE;T001");
        ok("SYSTEM DATE;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;10");
        ok("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(List.of("T001-SP1;SPEED;T001;8.00;true", "T001-SPECIAL;SPECIAL;T001;40.00;false"),
                Files.readAllLines(dataDir.resolve(TextPassRepository.FILE_NAME)));
        restart();
        Pass speed = passes().find("T001-SP1");
        Pass special = passes().find("T001-SPECIAL");
        assertTrue(speed.isConsumed());
        assertFalse(special.isConsumed());
        assertEquals(PassKind.SPECIAL, special.getKind());
        assertInstanceOf(SpecialPass.class, special);
        assertEquals("40.00", special.getPrice().toPlainString());
        assertSame(tickets().findIndividual("T001"), speed.getTicket());
    }

    @Test
    public void malformedPassLinesAreSkipped() throws Exception {
        Files.write(dataDir.resolve(TextPassRepository.FILE_NAME), List.of(
                "P1;SPEED;T001;8.00;false", "P2;OTHER;T001;8.00;false", "P3;SPEED;T999;8.00;false",
                "P4;SPEED;T001;x;false", "P5;SPEED;T001;8.00;maybe", "P6;SPEED"));
        restart();
        assertEquals(1, passes().getPasses().size());
        assertNull(passes().find("P2"));
    }
}
