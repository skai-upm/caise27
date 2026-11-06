package es.upm.park.command;

import es.upm.park.controller.ParkController;
import es.upm.park.controller.ParkControllerTestSupport;
import es.upm.park.controller.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupTicketCommandsTest {

    private static final String[] USERS = {"u1@correo.com", "u2@correo.com", "u3@correo.com",
        "u4@correo.com", "u5@correo.com", "u6@correo.com"};

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        ParkController.getInstance().setUserDirectory(TestUsers.of(USERS));
        run("GROUP CREATE;G02;Amigos");
        for (String user : USERS) {
            run("GROUP ADD;G02;" + user);
        }
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void sixMembersAtFortyEurosCost192() {
        assertEquals("GROUP TICKET CREATED. TOTAL PRICE: 192.00", run("GROUPTICKET CREATE;TG02;G02;2027-04-15"));
    }

    @Test
    void deleteFutureGroupTicket() {
        run("GROUPTICKET CREATE;TG03;G02;2027-04-20");
        assertTrue(run("GROUP ADD;G02;u1@correo.com").startsWith("ERROR"));
        assertEquals("GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG03"));
    }

    @Test
    void errorsAreReported() {
        run("GROUP CREATE;G03;Vacio");
        for (String line : new String[] {"GROUPTICKET CREATE;TG04;G03;2027-04-15",
                "GROUPTICKET CREATE;TG04;G02;2027-04-14", "GROUPTICKET CREATE;TG04;G02",
                "GROUPTICKET CREATE;TG04;G99;2027-04-15", "GROUPTICKET DELETE;TG99", "GROUPTICKET DELETE"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
        run("GROUPTICKET CREATE;TG05;G02;2027-04-15");
        assertTrue(run("GROUPTICKET DELETE;TG05").startsWith("ERROR"));
        assertTrue(run("TICKET DELETE;TG05:DNI1").startsWith("ERROR"));
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = run("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
        assertTrue(help.contains("GROUPTICKET DELETE;<identifier>"));
    }
}
