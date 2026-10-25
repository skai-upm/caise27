package com.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.TicketTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class StandardInvoiceBuilderTest {

    @Test
    public void emptyStepsAddNoSections() {
        Invoice invoice = new StandardInvoiceBuilder().forRecipient("X")
                .addIndividualTickets(List.of()).addGroupTickets(List.of())
                .addSpeedPasses(List.of()).addSpecialPasses(List.of()).build();
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        assertTrue(invoice.render().endsWith("TOTAL: 0.00"));
    }

    @Test
    public void stepsAddSectionsInOrderWithTotal() {
        IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"),
                TicketTest.user(1));
        Invoice invoice = new StandardInvoiceBuilder().forRecipient("USER a")
                .addIndividualTickets(List.of(ticket))
                .addSpeedPasses(ticket.buySpeedPasses(2))
                .build();
        assertEquals(2, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).title());
        assertEquals("SpeedPasses", invoice.getSections().get(1).title());
        assertEquals(new BigDecimal("56.00"), invoice.getTotal());
    }
}
