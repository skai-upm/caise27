package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassTest extends ParkTestSupport {

    @BeforeEach
    void tickets() {
        run(ANA);
        run(PEDRO);
        createUser(3);
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;u3@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) park.getTickets().find(id);
    }

    @Test
    void buysOneSpeedPassAtTwentyPercent() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00", run("SPEEDPASS CREATE;T001;1"));
        SpeedPass pass = ticket("T001").getSpeedPasses().get(0);
        assertEquals("T001-SPEED1", pass.getId());
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
        assertFalse(pass.isConsumed());
    }

    @Test
    void packOfTenCreatesTenIndependentPasses() {
        assertEquals("OK SPEEDPASS CREATED -> 80.00", run("SPEEDPASS CREATE;T001;10"));
        List<SpeedPass> passes = ticket("T001").getSpeedPasses();
        assertEquals(10, passes.size());
        assertEquals(10, passes.stream().map(SpeedPass::getId).distinct().count());
        assertTrue(passes.stream().noneMatch(SpeedPass::isConsumed));
        run("SPEEDPASS CREATE;T001;1");
        assertEquals("T001-SPEED11", ticket("T001").getSpeedPasses().get(10).getId());
    }

    @Test
    void constituentPassesUseTheConstituentPrice() {
        assertEquals("OK SPEEDPASS CREATED -> 7.20", run("SPEEDPASS CREATE;TG01-1;1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SPEEDPASS CREATE;T001;5", "SPEEDPASS CREATE;T001;0", "SPEEDPASS CREATE;T001;x",
            "SPEEDPASS CREATE;TG01;1", "SPEEDPASS CREATE;NOPE;1", "SPECIALPASS CREATE;TG01",
            "SPECIALPASS CREATE;NOPE"})
    void rejectsInvalidPurchases(String command) {
        assertTrue(error(run(command)));
        assertTrue(ticket("T001").getPasses().isEmpty());
    }

    @Test
    void rejectsPassesForPastTickets() {
        run("SYSTEM DATE;2026-10-16");
        assertTrue(error(run("SPEEDPASS CREATE;T001;1")));
        assertTrue(error(run("SPECIALPASS CREATE;T001")));
    }

    @Test
    void specialPassCostsTheTicketPriceOncePerLifetime() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00", run("SPECIALPASS CREATE;T001"));
        assertEquals("T001-SPECIAL", ticket("T001").getSpecialPass().getId());
        assertTrue(error(run("SPECIALPASS CREATE;T001")));

        ticket("T001").getSpecialPass().consume();
        assertTrue(error(run("SPECIALPASS CREATE;T001")));
    }

    @Test
    void refundedSpecialPassCannotBeBoughtAgain() throws IOException {
        run("SPECIALPASS CREATE;T001");
        assertTrue(ok(run("SPECIALPASS DELETE;T001-SPECIAL")));
        assertNull(ticket("T001").getSpecialPass());
        assertTrue(error(run("SPECIALPASS CREATE;T001")));
        restart();
        assertTrue(error(run("SPECIALPASS CREATE;T001")));
    }

    @Test
    void refundsOnlyUnusedPassesOfFutureTickets() {
        run("SPEEDPASS CREATE;T001;10");
        assertTrue(ok(run("SPEEDPASS DELETE;T001-SPEED1")));
        assertEquals(9, ticket("T001").getSpeedPasses().size());

        ticket("T001").getSpeedPasses().get(0).consume();
        assertTrue(error(run("SPEEDPASS DELETE;T001-SPEED2")));

        run("SYSTEM DATE;2026-10-15");
        assertTrue(error(run("SPEEDPASS DELETE;T001-SPEED3")));
        assertTrue(error(run("SPEEDPASS DELETE;NOPE")));
        assertEquals(9, ticket("T001").getSpeedPasses().size());
    }

    @Test
    void deleteCommandMustMatchThePassKind() {
        run("SPEEDPASS CREATE;T001;1");
        run("SPECIALPASS CREATE;T001");
        assertTrue(error(run("SPECIALPASS DELETE;T001-SPEED1")));
        assertTrue(error(run("SPEEDPASS DELETE;T001-SPECIAL")));
        assertEquals(2, ticket("T001").getPasses().size());
    }

    @Test
    void passesAreRestoredWithPriceTicketAndState() throws IOException {
        run("SPEEDPASS CREATE;T001;10");
        run("SPECIALPASS CREATE;T001");
        run("SPEEDPASS CREATE;TG01-2;1");
        ticket("T001").getSpeedPasses().get(0).consume();
        run("SPEEDPASS CREATE;T001;1");

        restart();

        IndividualTicket t001 = ticket("T001");
        assertEquals(11, t001.getSpeedPasses().size());
        assertTrue(t001.getSpeedPasses().get(0).isConsumed());
        assertFalse(t001.getSpeedPasses().get(1).isConsumed());
        assertEquals(new BigDecimal("8.00"), t001.getSpeedPasses().get(5).getPrice());
        assertNotNull(t001.getSpecialPass());
        assertEquals(new BigDecimal("40.00"), t001.getSpecialPass().getPrice());
        assertEquals(new BigDecimal("7.20"), ticket("TG01-2").getSpeedPasses().get(0).getPrice());
    }

    @Test
    void userInvoiceIncludesPassesOfConstituentsWithoutTheConstituent() {
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPECIALPASS CREATE;T001");
        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) 40.00"));
        assertTrue(invoice.contains("Personal passes:"));
        assertTrue(invoice.contains("SpeedPass TG01-1-SPEED1 (ticket TG01-1) 7.20"));
        assertTrue(invoice.contains("Special Pass T001-SPECIAL (ticket T001) 40.00"));
        assertFalse(invoice.contains("Ticket TG01-1 "));
        assertTrue(invoice.endsWith("TOTAL: 87.20"));
    }

    @Test
    void groupInvoiceExcludesMembersPasses() {
        run("SPEEDPASS CREATE;TG01-1;10");
        String invoice = run("INVOICE CREATE;GROUP;G01");
        assertFalse(invoice.contains("SpeedPass"));
        assertTrue(invoice.endsWith("TOTAL: 108.00"));
    }
}
