package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private TicketController service;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        service = park.getTicketController();
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
    }

    @Test
    void individualTicketUsesCurrentPrice() {
        IndividualTicket t1 = service.createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getSystemController().setPrice("45.50");
        IndividualTicket t2 = service.createIndividual("T002", "marta@correo.com", "2027-04-20");

        assertEquals(new BigDecimal("40.00"), t1.getPrice());
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
    }

    @Test
    void rejectsInvalidIndividualTickets() {
        service.createIndividual("T001", "ana@correo.com", "2027-04-15");

        assertThrows(ParkException.class, () -> service.createIndividual("T001", "pedro@correo.com", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createIndividual("T009", "ana@correo.com", "2027-04-14"));
        assertThrows(ParkException.class, () -> service.createIndividual("T009", "nobody@correo.com", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createIndividual("T009", "ana@correo.com", "15-04-2027"));
        assertThrows(ParkException.class, () -> service.createIndividual(" ", "ana@correo.com", "2027-04-15"));
        assertEquals(1, park.getRepositories().tickets().findAll().size());
    }

    @Test
    void groupTicketIsComposedOfMemberTickets() {
        GroupTicket ticket = service.createGroup("TG01", "G01", "2027-04-15");

        assertEquals(3, ticket.getTickets().size());
        assertTrue(park.getRepositories().tickets().findById("TG01:12345678Z").isPresent());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertEquals(park.getRepositories().groups().findById("G01").orElseThrow(), ticket.getGroup());
    }

    @Test
    void sixMembersAtFortyCost192() {
        park.getGroupController().create("G02", "Seis");
        TestPark.addGeneratedMembers(park, "G02", 6);

        assertEquals(new BigDecimal("192.00"), service.createGroup("TG02", "G02", "2027-04-15").getPrice());
    }

    @Test
    void sharedIdentifierSpace() {
        service.createGroup("TG01", "G01", "2027-04-15");
        service.createIndividual("TG03:12345678Z", "ana@correo.com", "2027-04-15");

        assertThrows(ParkException.class, () -> service.createIndividual("TG01", "ana@correo.com", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createIndividual("TG01:87654321X", "ana@correo.com", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createGroup("TG03", "G01", "2027-04-15"));
        assertTrue(park.getRepositories().tickets().findById("TG03").isEmpty());
    }

    @Test
    void rejectsInvalidGroupTickets() {
        park.getGroupController().create("G02", "Vacio");

        assertThrows(ParkException.class, () -> service.createGroup("TG09", "G02", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createGroup("TG09", "G99", "2027-04-15"));
        assertThrows(ParkException.class, () -> service.createGroup("TG09", "G01", "2027-04-14"));
        assertTrue(park.getRepositories().tickets().findAll().isEmpty());
    }

    @Test
    void compositionDoesNotFollowLaterGroupChanges() {
        GroupTicket ticket = service.createGroup("TG01", "G01", "2027-04-15");
        park.getSystemController().setDate("2027-04-16");
        park.getGroupController().removeMember("G01", "ana@correo.com");

        assertEquals(3, ticket.getTickets().size());
    }

    @Test
    void onlyFutureStandAloneTicketsCanBeDeleted() {
        service.createIndividual("T001", "ana@correo.com", "2027-04-15");
        service.createIndividual("T002", "marta@correo.com", "2027-04-20");
        service.createGroup("TG01", "G01", "2027-04-20");

        assertThrows(ParkException.class, () -> service.deleteIndividual("T001"));
        assertThrows(ParkException.class, () -> service.deleteIndividual("TG01:12345678Z"));
        assertThrows(ParkException.class, () -> service.deleteIndividual("TG01"));
        assertThrows(ParkException.class, () -> service.deleteIndividual("T404"));
        service.deleteIndividual("T002");

        assertTrue(park.getRepositories().tickets().findById("T002").isEmpty());
        assertTrue(park.getRepositories().tickets().findById("T001").isPresent());
    }

    @Test
    void onlyFutureGroupTicketsCanBeDeletedWithTheirComposition() {
        service.createGroup("TG01", "G01", "2027-04-15");
        service.createGroup("TG02", "G01", "2027-04-20");
        service.createIndividual("T001", "ana@correo.com", "2027-04-20");

        assertThrows(ParkException.class, () -> service.deleteGroup("TG01"));
        assertThrows(ParkException.class, () -> service.deleteGroup("T001"));
        service.deleteGroup("TG02");

        assertTrue(park.getRepositories().tickets().findById("TG02").isEmpty());
        assertTrue(park.getRepositories().tickets().findById("TG02:12345678Z").isEmpty());
        assertTrue(park.getRepositories().tickets().findById("TG01:12345678Z").isPresent());
    }
}
