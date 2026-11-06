package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.controller.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com"));
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void groupLifecycle() {
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        assertTrue(run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals("GROUP DELETED", run("GROUP DELETE;G01"));
        assertTrue(run("GROUP DELETE;G01").startsWith("ERROR"));
    }

    @Test
    void wrongArgumentsAreRejected() {
        for (String line : new String[] {"GROUP CREATE;G02", "GROUP CREATE;;Nombre", "GROUP ADD;G01",
                "GROUP REMOVE", "GROUP DELETE", "GROUP CREATE;G01;A;B"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
        assertTrue(ParkController.getInstance().findGroup("G02").isEmpty());
    }

    @Test
    void unknownUserIsRejected() {
        run("GROUP CREATE;G01;Familia Garcia");
        assertTrue(run("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
    }

    @Test
    void helpListsGroupCommands() {
        String help = run("HELP");
        assertTrue(help.contains("GROUP CREATE;<Identifier>;<Name>"));
        assertTrue(help.contains("GROUP ADD;<Identifier>;<Email>"));
        assertTrue(help.contains("GROUP REMOVE;<Identifier>;<Email>"));
        assertTrue(help.contains("GROUP DELETE;<Identifier>"));
    }
}
