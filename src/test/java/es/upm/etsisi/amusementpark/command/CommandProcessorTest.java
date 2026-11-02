package es.upm.etsisi.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.park.ParkFixture;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        park = ParkFixture.reset(dir);
        processor = new CommandProcessor(park);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createDeleteUpdate() {
        assertEquals("USER CREATED", run(ANA));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", park.getUsers().get(0).getFirstName());
        assertEquals("USER DELETED", run("  USER DELETE;ana@correo.com;Clave1!  "));
        assertTrue(park.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER CREATE;Ana;Garcia Lopez",
        "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;ana@correo.com",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "FOO BAR",
        "HELP;extra",
        "EXIT;now",
        "SYSTEM DATE;2027-02-30",
        "SYSTEM DATE;15/04/2027",
        "SYSTEM DATE",
        "SYSTEM PRICE;0",
        "SYSTEM PRICE;-5",
        "SYSTEM PRICE;abc",
        "SYSTEM PRICE;10.999",
        "GROUP CREATE;G01",
        "GROUP ADD;G99;ana@correo.com",
        "GROUP REMOVE;G99;ana@correo.com",
        "GROUP DELETE;G99",
        "TICKET CREATE;T1;nadie@correo.com;2027-04-15",
        "TICKET CREATE;T1;ana@correo.com;2027-04-14",
        "TICKET CREATE;T1;ana@correo.com;tomorrow",
        "TICKET DELETE;T9",
        "GROUPTICKET CREATE;TG;G99;2027-04-15",
        "GROUPTICKET DELETE;TG",
        "ATTRACTION CREATE;A01;Noria;0",
        "ATTRACTION CREATE;A01;Noria;abc",
        "ATTRACTION ENTER;A09;T1",
        "ATTRACTION EXIT;A09;ana@correo.com",
        "ATTRACTION LIST;A09",
        "ATTRACTION STATUS;A09",
        "ATTRACTION STATUS"
    })
    void rejectedCommandsReportErrorAndDoNotExit(String line) {
        run(ANA);
        CommandResult result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
        assertFalse(result.exit());
        assertEquals(1, park.getUsers().size());
        assertEquals("Ana", park.getUsers().get(0).getFirstName());
        assertEquals(ParkFixture.TODAY, park.getCurrentDate());
        assertEquals(Park.INITIAL_PRICE, park.getBasePrice());
        assertTrue(park.getTickets().isEmpty());
        assertTrue(park.getAttractions().isEmpty());
        assertTrue(park.getGroups().isEmpty());
    }

    @Test
    void systemCommands() {
        assertEquals("SYSTEM DATE SET TO 2027-05-01", run("SYSTEM DATE;2027-05-01"));
        assertEquals("SYSTEM PRICE SET TO 45.50 EUR", run("SYSTEM PRICE;45.50"));
        run(ANA);
        assertEquals("TICKET CREATED; PRICE: 45.50 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-05-01"));
    }

    @Test
    void groupAndTicketFlow() {
        run(ANA);
        run(PEDRO);
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;pedro@correo.com"));
        assertTrue(run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR:"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;pedro@correo.com"));
        run("GROUP ADD;G01;pedro@correo.com");
        assertEquals("GROUP TICKET CREATED; MEMBERS: 2; TOTAL PRICE: 80.00 EUR",
                run("GROUPTICKET CREATE;TG01;G01;2027-04-16"));
        assertTrue(run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR:"));
        assertTrue(run("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR:"));
        assertTrue(run("TICKET DELETE;TG01:87654321X").startsWith("ERROR:"));
        assertEquals("GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG01"));
        assertEquals("GROUP DELETED", run("GROUP DELETE;G01"));
        assertEquals("TICKET CREATED; PRICE: 40.00 EUR", run("TICKET CREATE;T2;ana@correo.com;2027-04-20"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T2"));
    }

    @Test
    void attractionFlow() {
        run(ANA);
        run(PEDRO);
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("NO USERS IN A01", run("ATTRACTION LIST;A01"));
        assertEquals("ENTERED A01: ana@correo.com, pedro@correo.com", run("ATTRACTION ENTER;A01;TG01"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), run("ATTRACTION LIST;A01").lines().toList());
        assertEquals("USER LEFT A01", run("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals(List.of("ATTRACTION: A01", "NAME: Dragon Rojo", "CAPACITY: 20", "INSIDE: 1",
                "AVAILABLE: 19", "USERS: pedro@correo.com"), run("ATTRACTION STATUS;A01").lines().toList());
        run("ATTRACTION EXIT;A01;pedro@correo.com");
        assertTrue(run("ATTRACTION STATUS;A01").endsWith("USERS: -"));
    }

    @Test
    void helpListsAllCommands() {
        String help = run("HELP");
        for (String syntax : List.of(
                "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                "USER DELETE;<email>;<password>",
                "USER UPDATE;<email>;<password>;<field>;<value>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
                "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>",
                "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT")) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
    }
}
