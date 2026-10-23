package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ticket deletion refunds the passes attached to the deleted tickets.
 */
class PassCascadeTest {

    @TempDir
    Path dir;

    private Park park;
    private PassController passes;
    private TicketController tickets;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        passes = PassController.getInstance();
        tickets = TicketController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G02", "Visita Mayo");
        groups.addMember("G02", "pedro@correo.com");
        groups.addMember("G02", "laura@correo.com");
    }

    @Test
    void ticketDeletionRemovesPassesFromAllPurchases() throws ParkException {
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
        tickets.createTicket("T003", "ana@correo.com", TODAY.plusDays(5));
        passes.buySpeedPasses("SP01", "T002", 1);
        passes.buySpeedPasses("SP02", "T002", 10);
        passes.buySpecialPass("PE01", "T002");
        passes.buySpeedPasses("SP03", "T003", 1);

        tickets.deleteTicket("T002");

        assertNull(park.findPass("SP01:1"));
        assertNull(park.findPass("SP02:7"));
        assertNull(park.findPass("PE01"));
        assertNotNull(park.findPass("SP03:1"));
        assertNull(ParkFixture.restart(dir).findPass("SP02:1"));
    }

    @Test
    void groupTicketDeletionRemovesMembersPasses() throws ParkException {
        tickets.createGroupTicket("TG02", "G02", TODAY.plusDays(5));
        passes.buySpeedPasses("SP04", "TG02:87654321X", 10);
        passes.buySpecialPass("PE04", "TG02:11111111H");

        tickets.deleteGroupTicket("TG02");

        assertNull(park.findTicket("TG02:87654321X"));
        assertNull(park.findPass("SP04:1"));
        assertNull(park.findPass("PE04"));
    }

    @Test
    void rejectedDeletionKeepsPasses() throws ParkException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        passes.buySpeedPasses("SP01", "T001", 1);
        assertThrows(ParkException.class, () -> tickets.deleteTicket("T001"));
        assertNotNull(park.findPass("SP01:1"));
    }
}
