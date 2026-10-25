package com.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PassTest {

    private final IndividualTicket ticket =
            new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), TicketTest.user(1));

    @Test
    public void speedPassesCostTwentyPercentAndAreIndependent() {
        List<SpeedPass> bought = ticket.buySpeedPasses(10);
        assertEquals(10, bought.size());
        assertEquals("T001-SP1", bought.get(0).getId());
        assertEquals("T001-SP10", bought.get(9).getId());
        for (SpeedPass speedPass : bought) {
            assertEquals(new BigDecimal("8.00"), speedPass.getPrice());
            assertFalse(speedPass.isConsumed());
        }
        bought.get(0).consume();
        assertTrue(bought.get(0).isConsumed());
        assertFalse(bought.get(1).isConsumed());
        assertSame(bought.get(1), ticket.findUnusedSpeedPass());
    }

    @Test
    public void speedPassNumbersContinueAfterRefund() {
        List<SpeedPass> first = ticket.buySpeedPasses(1);
        ticket.buySpeedPasses(1);
        ticket.remove(first.get(0));
        assertEquals("T001-SP3", ticket.buySpeedPasses(1).get(0).getId());
    }

    @Test
    public void onlyOneSpecialPassAtTicketPrice() {
        SpecialPass specialPass = ticket.buySpecialPass();
        assertEquals("T001-SPECIAL", specialPass.getId());
        assertEquals(new BigDecimal("40.00"), specialPass.getPrice());
        specialPass.consume();
        assertNull(ticket.buySpecialPass());
        assertSame(specialPass, ticket.findPass("T001-SPECIAL"));
    }

    @Test
    public void speedPassPriceRounding() {
        assertEquals(new BigDecimal("12.35"), SpeedPass.priceFor(new BigDecimal("61.75")));
    }
}
