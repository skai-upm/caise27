package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;

import org.junit.jupiter.api.Test;

class PassCommandsTest extends ParkTestSupport {

    private IndividualTicket ticket(String id) {
        return park.findIndividualTicket(id);
    }

    @Test
    void buysSingleSpeedPass() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        expect("OK SPEEDPASS CREATED -> 8.00 [T001-SP1]", "SPEEDPASS CREATE;T001;1");
        SpeedPass pass = ticket("T001").getSpeedPasses().get(0);
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
        assertFalse(pass.isConsumed());
    }

    @Test
    void packOfTenCreatesTenIndependentPasses() {
        createUsers(1);
        ok("SYSTEM PRICE;60.00");
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        String output = run("SPEEDPASS CREATE;T001;10");
        assertTrue(output.startsWith("OK SPEEDPASS CREATED -> 120.00 [T001-SP1, "), output);
        List<SpeedPass> passes = ticket("T001").getSpeedPasses();
        assertEquals(10, passes.size());
        assertEquals(10, passes.stream().map(Pass::getId).distinct().count());
        assertTrue(passes.stream().allMatch(p -> p.getPrice().compareTo(new BigDecimal("12.00")) == 0 && !p.isConsumed()));
        ok("SPEEDPASS CREATE;T001;1");
        assertEquals("T001-SP11", ticket("T001").getSpeedPasses().get(10).getId());
    }

    @Test
    void rejectsInvalidSpeedPassRequests() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        error("SPEEDPASS CREATE;T001;5");
        error("SPEEDPASS CREATE;T001;0");
        error("SPEEDPASS CREATE;T001;ten");
        error("SPEEDPASS CREATE;T999;1");
        ok("SYSTEM DATE;2026-10-16");
        error("SPEEDPASS CREATE;T001;1");
        assertTrue(ticket("T001").getSpeedPasses().isEmpty());
    }

    @Test
    void specialPassOncePerTicketLifetime() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        expect("OK SPECIALPASS CREATED -> 40.00 [T001-SPECIAL]", "SPECIALPASS CREATE;T001");
        error("SPECIALPASS CREATE;T001");
        ticket("T001").getSpecialPass().consume();
        error("SPECIALPASS CREATE;T001");
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
    }

    @Test
    void refundedSpecialPassCannotBeBoughtAgain() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SPECIALPASS CREATE;T001");
        expect("OK: Special Pass T001-SPECIAL deleted", "SPECIALPASS DELETE;T001-SPECIAL");
        assertNull(ticket("T001").getSpecialPass());
        error("SPECIALPASS CREATE;T001");
    }

    @Test
    void passesForGroupConstituents() {
        createUsers(3);
        createGroupWithUsers("G01", 3);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        expect("OK SPEEDPASS CREATED -> 7.20 [TG01-1-SP1]", "SPEEDPASS CREATE;TG01-1;1");
        expect("OK SPECIALPASS CREATED -> 36.00 [TG01-2-SPECIAL]", "SPECIALPASS CREATE;TG01-2");
        error("SPEEDPASS CREATE;TG01;1");
    }

    @Test
    void onlyUnusedPassesOfFutureTicketsAreRefundable() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SPEEDPASS CREATE;T001;10");
        ticket("T001").getSpeedPasses().get(1).consume();
        expect("OK: SpeedPass T001-SP1 deleted", "SPEEDPASS DELETE;T001-SP1");
        error("SPEEDPASS DELETE;T001-SP2");
        error("SPEEDPASS DELETE;T001-SP99");
        error("SPECIALPASS DELETE;T001-SP3");
        ok("SYSTEM DATE;2026-10-15");
        error("SPEEDPASS DELETE;T001-SP3");
        assertEquals(9, ticket("T001").getSpeedPasses().size());
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
        assertNotNull(ticket("T001"));
    }

    @Test
    void userInvoiceIncludesPassesWithoutDoubleCountingConstituents() {
        createUsers(3);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        createGroupWithUsers("G01", 3);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");

        String invoice = run("INVOICE CREATE;USER;" + email(1));
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) 40.00"), invoice);
        assertTrue(invoice.contains("Passes:"), invoice);
        assertTrue(invoice.contains("SpeedPass TG01-1-SP1 (ticket TG01-1) 7.20"), invoice);
        assertFalse(invoice.contains("Ticket TG01"), invoice);
        assertTrue(invoice.endsWith("TOTAL: 47.20"), invoice);

        String groupInvoice = run("INVOICE CREATE;GROUP;G01");
        assertFalse(groupInvoice.contains("SpeedPass"), groupInvoice);
        assertTrue(groupInvoice.endsWith("TOTAL: 108.00"), groupInvoice);
    }

    @Test
    void invoiceWithoutPassesOmitsPassesSection() {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        assertFalse(run("INVOICE CREATE;USER;" + email(1)).contains("Passes:"));
    }
}
