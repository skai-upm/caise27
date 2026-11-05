package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.model.Group;
import es.upm.parque.park.ParkController;
import es.upm.parque.repository.UserRepository;

class GroupCommandsTest {

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserRepository());
        park = ParkController.getInstance();
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457");
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000002;1234567890123458");
    }

    private Group g01() {
        return park.getGroups().findById("G01");
    }

    @Test
    void createsEmptyGroup() {
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").startsWith("OK"));
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(g01().getMembers().isEmpty());
    }

    @Test
    void rejectsDuplicateOrMalformedCreation() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        assertTrue(processor.process("GROUP CREATE;G01;Otro").startsWith("ERROR"));
        assertEquals("Familia Garcia", g01().getName());
        for (String bad : new String[] {"GROUP CREATE;;Nombre", "GROUP CREATE;G02;", "GROUP CREATE;G02"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertNull(park.getGroups().findById("G02"));
    }

    @Test
    void addsUsersProgressivelyWithoutDuplicates() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;pedro@correo.com").startsWith("OK"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertEquals(2, g01().getMembers().size());
        assertTrue(processor.process("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01").startsWith("ERROR"));
        assertEquals(2, g01().getMembers().size());
    }

    @Test
    void removesMembersOnly() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("GROUP REMOVE;G01;luis@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK"));
        assertTrue(g01().getMembers().isEmpty());
    }

    @Test
    void deletesGroups() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        assertTrue(processor.process("GROUP DELETE;G99").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        assertNull(g01());
    }

    @Test
    void membershipSurvivesEmailChangeAndUserDeletionLeavesGroups() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com");
        assertTrue(processor.process("GROUP ADD;G01;ana.nueva@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Clave1!").startsWith("OK"));
        assertTrue(g01().getMembers().isEmpty());
    }

    @Test
    void groupsWithoutTicketsAreNotLockedAndResetPerExecution() {
        processor.process("GROUP CREATE;G01;Familia Garcia");
        assertNotNull(g01());
        assertFalse(park.hasActiveGroupTicket(g01()));
        new CommandProcessor(new UserRepository());
        assertTrue(park.getGroups().findAll().isEmpty());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String syntax : new String[] {"GROUP CREATE;<id>;<name>", "GROUP ADD;<id>;<email>",
                "GROUP REMOVE;<id>;<email>", "GROUP DELETE;<id>"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }
}
