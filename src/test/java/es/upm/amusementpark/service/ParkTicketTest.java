package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.ticket.GroupTicket;
import es.upm.amusementpark.model.ticket.IndividualTicket;
import es.upm.amusementpark.model.ticket.Ticket;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkTicketTest extends ParkTestSupport {

    private User ana;
    private User pedro;

    @BeforeEach
    void users() throws Exception {
        ana = createAna();
        pedro = createPedro();
    }

    @Test
    void createsTicketWithDefaultPrice() throws Exception {
        IndividualTicket ticket = park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        assertSame(ana, ticket.getUser());
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertSame(ticket, park.findTicket("T001"));
    }

    @Test
    void priceChangeAffectsOnlyNewTickets() throws Exception {
        Ticket first = park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.setCurrentPrice("45.50");
        Ticket second = park.createTicket("T002", "ana@correo.com", TODAY_TEXT);
        assertEquals(new BigDecimal("40.00"), first.getPrice());
        assertEquals(new BigDecimal("45.50"), second.getPrice());
    }

    @Test
    void rejectsInvalidTicketCreation() throws Exception {
        assertThrows(OperationException.class, () -> park.createTicket("T002", "ana@correo.com", YESTERDAY_TEXT));
        assertThrows(OperationException.class, () -> park.createTicket("T003", "nadie@correo.com", TODAY_TEXT));
        assertThrows(OperationException.class, () -> park.createTicket("T004", "ana@correo.com", "2027-02-30"));
        assertThrows(OperationException.class, () -> park.createTicket("", "ana@correo.com", TODAY_TEXT));
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        assertThrows(OperationException.class, () -> park.createTicket("T001", "pedro@correo.com", TODAY_TEXT));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void sharedIdentifierSpace() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        assertThrows(OperationException.class, () -> park.createTicket("TG01", "ana@correo.com", TODAY_TEXT));
        assertThrows(OperationException.class,
                () -> park.createTicket("TG01:12345678Z", "ana@correo.com", TODAY_TEXT));
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroup("G02", "Otro");
        park.addToGroup("G02", "pedro@correo.com");
        assertThrows(OperationException.class, () -> park.createGroupTicket("T001", "G02", TODAY_TEXT));
    }

    @Test
    void deletesOnlyFutureStandaloneTickets() throws Exception {
        park.createTicket("T001", "ana@correo.com", TOMORROW_TEXT);
        park.createTicket("T002", "ana@correo.com", TODAY_TEXT);
        park.deleteTicket("T001");
        assertNull(park.findTicket("T001"));

        assertThrows(OperationException.class, () -> park.deleteTicket("T002"));
        park.setCurrentDate("2027-04-20");
        assertThrows(OperationException.class, () -> park.deleteTicket("T002"));
        assertNotNull(park.findTicket("T002"));
        assertThrows(OperationException.class, () -> park.deleteTicket("T999"));
    }

    @Test
    void compositionTicketCannotBeDeletedAlone() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TOMORROW_TEXT);
        assertThrows(OperationException.class, () -> park.deleteTicket("TG01:12345678Z"));
        assertThrows(OperationException.class, () -> park.deleteTicket("TG01"));
        assertNotNull(park.findTicket("TG01:12345678Z"));
    }

    @Test
    void createsGroupTicketComposition() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");

        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY_TEXT);

        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertEquals(2, ticket.getComposition().size());
        IndividualTicket anaTicket = (IndividualTicket) park.findTicket("TG01:12345678Z");
        IndividualTicket pedroTicket = (IndividualTicket) park.findTicket("TG01:87654321X");
        assertSame(ana, anaTicket.getUser());
        assertSame(pedro, pedroTicket.getUser());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void sixMembersAtFortyCost192() throws Exception {
        createGroupWith("G02", 1, 6);
        GroupTicket ticket = park.createGroupTicket("TG02", "G02", TODAY_TEXT);
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        for (IndividualTicket member : ticket.getComposition()) {
            assertEquals(new BigDecimal("32.00"), member.getPrice());
        }
    }

    @Test
    void groupDiscountUsesCurrentPrice() throws Exception {
        createGroupWith("G03", 1, 3);
        park.setCurrentPrice("50");
        assertEquals(new BigDecimal("135.00"), park.createGroupTicket("TG03", "G03", TODAY_TEXT).getPrice());
    }

    @Test
    void rejectsInvalidGroupTickets() throws Exception {
        park.createGroup("G01", "Vacio");
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG01", "G01", TODAY_TEXT));
        park.addToGroup("G01", "ana@correo.com");
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG01", "G01", YESTERDAY_TEXT));
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG01", "G99", TODAY_TEXT));
        assertEquals(0, park.getTickets().size());
    }

    @Test
    void derivedIdentifierConflictCreatesNothing() throws Exception {
        park.createTicket("TG01:87654321X", "pedro@correo.com", TODAY_TEXT);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");

        assertThrows(OperationException.class, () -> park.createGroupTicket("TG01", "G01", TODAY_TEXT));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void compositionIsFixed() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);
        park.addToGroup("G01", "pedro@correo.com");
        assertEquals(1, ticket.getComposition().size());
    }

    @Test
    void deletesFutureGroupTicketWithComposition() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TOMORROW_TEXT);
        park.createGroupTicket("TG02", "G01", TODAY_TEXT);

        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertNull(park.findTicket("TG01:87654321X"));

        assertThrows(OperationException.class, () -> park.deleteGroupTicket("TG02"));
        assertNotNull(park.findTicket("TG02:12345678Z"));
        park.createTicket("T001", "ana@correo.com", TOMORROW_TEXT);
        assertThrows(OperationException.class, () -> park.deleteGroupTicket("T001"));
    }

    @Test
    void ticketsFoundByTraversal() throws Exception {
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        assertEquals(2, park.ticketsOf(ana).size());
        assertEquals(0, park.ticketsOf(pedro).size());
        assertEquals(1, park.groupTicketsOf(park.findGroup("G01")).size());

        for (Field field : User.class.getDeclaredFields()) {
            assertFalse(field.getGenericType().getTypeName().contains("ticket"), field.getName());
        }
    }
}
