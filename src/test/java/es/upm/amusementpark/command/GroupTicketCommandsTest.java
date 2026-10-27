package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.repository.UserRepository;

class GroupTicketCommandsTest {

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.start(new UserRepository());
        processor = new CommandProcessor(park);
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444");
        processor.process("SYSTEM DATE;2027-04-10");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
    }

    private GroupTicket ticket(String id) {
        return (GroupTicket) park.getTickets().findById(id);
    }

    @Test
    void buysGroupTicket() {
        assertEquals("GROUPTICKET CREATED - Total price: 80.00 EUR", processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        GroupTicket t = ticket("TG01");
        assertEquals(park.getGroups().findById("G01"), t.getGroup());
        assertEquals("TG01:12345678Z", t.getComposition().get(0).getId());
        assertEquals("TG01:87654321X", t.getComposition().get(1).getId());
    }

    @Test
    void rejectsInvalidPurchases() {
        processor.process("GROUP CREATE;G02;Vacio");
        assertEquals("ERROR: Group has no members", processor.process("GROUPTICKET CREATE;TG02;G02;2027-04-15"));
        assertTrue(processor.process("GROUPTICKET CREATE;TG03;G01;2027-04-09").startsWith("ERROR"));
        assertEquals("ERROR: Group not found", processor.process("GROUPTICKET CREATE;TG04;G99;2027-04-15"));
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("ERROR: Ticket identifier already exists", processor.process("GROUPTICKET CREATE;T001;G01;2027-04-15"));
        assertNull(park.getTickets().findById("TG02"));
        assertNull(park.getTickets().findById("TG03"));
    }

    @Test
    void derivedIdentifierConflictPreventsCreation() {
        processor.process("TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-15");
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15").startsWith("ERROR: Ticket identifier already exists"));
        assertNull(park.getTickets().findById("TG01"));
        assertEquals(1, park.getTickets().findAll().size());
    }

    @Test
    void componentIdentifiersOccupyTheSharedSpace() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals("ERROR: Ticket identifier already exists",
                processor.process("TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-16"));
    }

    @Test
    void componentsCannotBeDeletedIndividually() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(processor.process("TICKET DELETE;TG01:12345678Z").startsWith("ERROR"));
        assertEquals(2, ticket("TG01").getComposition().size());
    }

    @Test
    void deletesOnlyFutureGroupTickets() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("GROUPTICKET CREATE;TG02;G01;2027-04-10");
        assertEquals("GROUPTICKET DELETED", processor.process("GROUPTICKET DELETE;TG01"));
        assertNull(park.getTickets().findById("TG01"));
        assertTrue(park.getTickets().isIdAvailable("TG01:12345678Z"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG02").startsWith("ERROR"));
        assertNotNull(ticket("TG02"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG99").startsWith("ERROR"));
    }

    @Test
    void activeGroupTicketLocksGroup() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(processor.process("GROUP ADD;G01;luis@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertEquals(2, park.getGroups().findById("G01").getMembers().size());
        assertEquals("GROUPTICKET DELETED", processor.process("GROUPTICKET DELETE;TG01"));
        assertEquals("GROUP USER ADDED", processor.process("GROUP ADD;G01;luis@correo.com"));
    }

    @Test
    void lockReleasedWhenTicketBecomesHistorical() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertEquals("GROUP USER REMOVED", processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(2, ticket("TG01").getComposition().size());
    }

    @Test
    void deletingGroupKeepsHistoricalTicketWithoutGroup() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-10");
        processor.process("SYSTEM DATE;2027-04-11");
        assertEquals("GROUP DELETED", processor.process("GROUP DELETE;G01"));
        GroupTicket t = ticket("TG01");
        assertNull(t.getGroup());
        assertEquals(2, t.getComposition().size());
        assertEquals("80.00", t.getPrice().toPlainString());
    }

    @Test
    void userDeletionRespectsGroupTickets() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(park.getGroups().findById("G01").contains(park.getUsers().findByEmail("ana@correo.com")));
        processor.process("GROUPTICKET DELETE;TG01");
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(1, park.getGroups().findById("G01").getMembers().size());
    }

    @Test
    void lockedGroupBlocksMemberDeletionEvenWithoutOwnActiveTicket() {
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals(true, park.isInLockedGroup(park.getUsers().findByEmail("pedro@correo.com")));
        assertTrue(processor.process("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR"));
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
        assertTrue(help.contains("GROUPTICKET DELETE;<identifier>"));
    }
}
