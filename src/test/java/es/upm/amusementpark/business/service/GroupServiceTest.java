package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupServiceTest {

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()), new GroupService(park.users(), park.groups()), new es.upm.amusementpark.business.service.TicketService(park.users(), park.tickets()));
        p.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        p.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
    }

    private Group g01() {
        return park.groups().findById("G01").orElseThrow();
    }

    @Test
    void createEmptyAndRejectDuplicate() {
        assertEquals("GROUP CREATED", p.process("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(g01().isEmpty());
        assertTrue(p.process("GROUP CREATE;G01;Otro").startsWith("ERROR"));
        assertEquals("Familia Garcia", g01().getName());
        assertTrue(p.process("GROUP CREATE;;X").startsWith("ERROR"));
    }

    @Test
    void addProgressivelyNoDuplicates() {
        p.process("GROUP CREATE;G01;Familia Garcia");
        assertEquals("USER ADDED TO GROUP", p.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("USER ADDED TO GROUP", p.process("GROUP ADD;G01;pedro@correo.com"));
        assertTrue(p.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertEquals(2, g01().getMembers().size());
        assertTrue(p.process("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(p.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        p.process("GROUP CREATE;G02;Amigos");
        assertEquals("USER ADDED TO GROUP", p.process("GROUP ADD;G02;ana@correo.com"));
    }

    @Test
    void removeMembers() {
        p.process("GROUP CREATE;G01;Familia Garcia");
        p.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(p.process("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertEquals("USER REMOVED FROM GROUP", p.process("GROUP REMOVE;G01;ana@correo.com"));
        assertTrue(g01().isEmpty());
    }

    @Test
    void deletedUserLeavesGroups() {
        p.process("GROUP CREATE;G01;Familia Garcia");
        p.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("USER DELETED", p.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(g01().isEmpty());
    }
}
