package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("ana@correo.com", "12345678Z");
    private final Ticket ticket = new IndividualTicket("T001", ana, DAY, new BigDecimal("40.00"));

    @Test
    void commonDataThroughAbstraction() {
        assertEquals("T001", ticket.getIdentifier());
        assertEquals(DAY, ticket.getDate());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(List.of("T001"), List.copyOf(ticket.identifiers()));
    }

    @Test
    void activeBoundaries() {
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertTrue(ticket.isActiveOn(DAY));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));
    }

    @Test
    void deletableOnlyStrictlyBefore() {
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));
        assertFalse(ticket.isDeletableOn(DAY));
        assertFalse(ticket.isDeletableOn(DAY.plusDays(1)));
    }

    @Test
    void ticketKnowsItsUser() {
        assertSame(ana, ((IndividualTicket) ticket).getUser());
        assertTrue(ticket.involves(new User("ana@correo.com", "12345678Z")));
        assertFalse(ticket.involves(new User("pedro@correo.com", "87654321X")));
    }

    @Test
    void userHoldsNoTicketReferences() {
        for (Field field : User.class.getDeclaredFields()) {
            assertFalse(Ticket.class.isAssignableFrom(field.getType()));
            assertFalse(java.util.Collection.class.isAssignableFrom(field.getType()));
        }
    }
}
