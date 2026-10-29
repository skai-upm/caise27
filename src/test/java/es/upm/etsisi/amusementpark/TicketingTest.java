package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketingTest extends ParkFixture {

    @Test
    void createIndividualTicket() throws Exception {
        IndividualTicket ticket = park.createTicket("T001", ANA, "2027-04-15");
        assertSame(user(ANA), ticket.getUser());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertSame(ticket, park.findTicket("T001"));
    }

    @Test
    void individualTicketRejections() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        assertThrows(ParkException.class, () -> park.createTicket("T001", PEDRO, "2027-04-15"));
        assertThrows(ParkException.class, () -> park.createTicket("T002", ANA, "2027-04-14"));
        assertThrows(ParkException.class, () -> park.createTicket("T003", "nadie@correo.com", "2027-04-15"));
        assertThrows(ParkException.class, () -> park.createTicket("T004", ANA, "15/04/2027"));
        assertThrows(ParkException.class, () -> park.createTicket("", ANA, "2027-04-15"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void deleteOnlyStrictlyFutureTickets() throws Exception {
        park.createTicket("T001", ANA, "2027-04-15");
        park.createTicket("T005", ANA, "2027-04-20");
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        park.deleteTicket("T005");
        assertNull(park.findTicket("T005"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T005"));
        park.setCurrentDate("2027-04-16");
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertNotNull(park.findTicket("T001"));
    }

    @Test
    void createGroupTicketWithComposition() throws Exception {
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertInstanceOf(IndividualTicket.class, park.findTicket("TG01:12345678Z"));
        assertSame(user(PEDRO), ((IndividualTicket) park.findTicket("TG01:87654321X")).getUser());
    }

    @Test
    void groupTicketRejections() throws Exception {
        park.createGroup("G01", "Vacio");
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        park.addToGroup("G01", ANA);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G99", "2027-04-15"));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-14"));
        park.createTicket("T001", PEDRO, "2027-04-15");
        assertThrows(ParkException.class, () -> park.createGroupTicket("T001", "G01", "2027-04-15"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void derivedIdentifierConflictCreatesNothing() throws Exception {
        park.createTicket("TG01:87654321X", PEDRO, "2027-04-16");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-16"));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void individualTicketIdCannotReuseCompositionId() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-16");
        assertThrows(ParkException.class, () -> park.createTicket("TG01:12345678Z", ANA, "2027-04-16"));
    }

    @Test
    void compositionFixedAfterGroupChanges() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.addToGroup("G01", PEDRO);
        assertEquals(List.of(user(ANA)), ticket.getUsers());
    }

    @Test
    void discountAppliedOnCurrentPrice() throws Exception {
        park.createGroup("G02", "Grande");
        addMembers("G02", 6);
        park.setCurrentPrice("40");
        assertEquals(new BigDecimal("192.00"), park.createGroupTicket("TG02", "G02", "2027-04-15").getPrice());
    }

    @Test
    void groupTicketDeletion() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createGroupTicket("TG02", "G01", "2027-04-20");
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG02:12345678Z"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG02"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG02:12345678Z"));
        park.deleteGroupTicket("TG02");
        assertNull(park.findTicket("TG02"));
        assertNull(park.findTicket("TG02:12345678Z"));
        assertNotNull(park.findTicket("TG01:12345678Z"));
    }
}
