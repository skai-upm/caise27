package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(email -> Set.of("ana@correo.com", "pedro@correo.com").contains(email));
    }

    @Test
    void ticketRulesOnDates() {
        Ticket ticket = new IndividualTicket("T1", "ana@correo.com", TODAY, new BigDecimal("40.00"));
        assertTrue(ticket.isActiveOn(TODAY));
        assertFalse(ticket.isActiveOn(TODAY.plusDays(1)));
        assertFalse(ticket.isDeletableOn(TODAY));
        assertTrue(ticket.isDeletableOn(TODAY.minusDays(1)));
    }

    @Test
    void createdAtInitialPrice() {
        Ticket ticket = park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(TODAY, ticket.getDate());
        assertEquals("ana@correo.com", ((IndividualTicket) ticket).getUserEmail());
    }

    @Test
    void usesConfiguredPriceAndKeepsItAfterChange() {
        park.setCurrentPrice(new BigDecimal("45.50"));
        Ticket ticket = park.createIndividualTicket("T001", "ana@correo.com", TODAY.plusDays(2));
        park.setCurrentPrice(new BigDecimal("50"));
        assertEquals(new BigDecimal("45.50"), ticket.getPrice());
    }

    @Test
    void identifierSpaceIsShared() {
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T001", "pedro@correo.com", TODAY));
        assertEquals("ana@correo.com", ((IndividualTicket) park.findTicket("T001")).getUserEmail());
        assertThrows(ParkException.class, () -> park.createIndividualTicket("", "pedro@correo.com", TODAY));
    }

    @Test
    void pastDateAndUnknownUserRejected() {
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T002", "ana@correo.com", TODAY.minusDays(1)));
        assertThrows(ParkException.class, () -> park.createIndividualTicket("T003", "nobody@correo.com", TODAY));
        assertNull(park.findTicket("T002"));
        assertNull(park.findTicket("T003"));
    }

    @Test
    void onlyFutureTicketsCanBeDeleted() {
        park.createIndividualTicket("T001", "ana@correo.com", TODAY.plusDays(5));
        park.createIndividualTicket("T002", "ana@correo.com", TODAY);
        park.deleteTicket("T001");
        assertNull(park.findTicket("T001"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T002"));
        assertNotNull(park.findTicket("T002"));
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.deleteTicket("T002"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T999"));
    }

    @Test
    void activeTicketsBlockUserDeletion() {
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        assertThrows(ParkException.class, () -> park.ensureUserDeletable("ana@correo.com"));
        park.ensureUserDeletable("pedro@correo.com");
        park.setCurrentDate(TODAY.plusDays(1));
        park.ensureUserDeletable("ana@correo.com");
        assertNotNull(park.findTicket("T001"));
    }
}
