package es.upm.park.command;

import es.upm.park.controller.ParkController;
import es.upm.park.controller.ParkControllerTestSupport;
import es.upm.park.controller.TestUsers;
import es.upm.park.model.Attraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createAndListEmptyAttraction() {
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        Attraction attraction = ParkController.getInstance().findAttraction("A01").orElseThrow();
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertEquals("NO USERS IN A01", run("ATTRACTION LIST;A01"));
        assertTrue(attraction.getOccupantEmails().isEmpty());
    }

    @Test
    void listShowsOccupantsWithoutChangingThem() {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        Attraction attraction = ParkController.getInstance().findAttraction("A01").orElseThrow();
        attraction.enter("ana@correo.com");
        attraction.enter("pedro@correo.com");
        String out = run("ATTRACTION LIST;A01");
        assertTrue(out.contains("ana@correo.com"));
        assertTrue(out.contains("pedro@correo.com"));
        assertEquals(2, attraction.getOccupantEmails().size());
    }

    @Test
    void invalidInputIsRejected() {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        for (String line : new String[] {"ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;A02;Torre;0",
                "ATTRACTION CREATE;A02;Torre;-3", "ATTRACTION CREATE;A02;Torre;doce", "ATTRACTION CREATE;A02;;12",
                "ATTRACTION CREATE;A02;Torre", "ATTRACTION CREATE;A02;Torre;1.5", "ATTRACTION LIST;A99",
                "ATTRACTION LIST"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
        assertEquals("Dragon Rojo", ParkController.getInstance().findAttraction("A01").orElseThrow().getName());
        assertTrue(ParkController.getInstance().findAttraction("A02").isEmpty());
    }

    @Test
    void enterWithIndividualTicket() {
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com"));
        run("SYSTEM DATE;2027-04-15");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("TICKET CREATE;T002;ana@correo.com;2027-04-16");
        assertTrue(run("ATTRACTION ENTER;A01;T002").startsWith("ERROR"));
        assertEquals("ENTERED A01: ana@correo.com", run("ATTRACTION ENTER;A01;T001"));
        assertTrue(run("ATTRACTION LIST;A01").contains("ana@correo.com"));
        run("SYSTEM DATE;2027-04-16");
        assertTrue(run("ATTRACTION LIST;A01").contains("ana@correo.com"));
        assertTrue(run("ATTRACTION ENTER;A01").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
    }

    @Test
    void statusAfterMixedEntriesAndExit() {
        ParkController.getInstance().setUserDirectory(
                TestUsers.of("ana@correo.com", "pedro@correo.com", "luis@correo.com"));
        run("SYSTEM DATE;2027-04-15");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUP CREATE;G01;Amigos");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;luis@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("ATTRACTION ENTER;A01;T001");
        assertEquals("ENTERED A01: pedro@correo.com, luis@correo.com", run("ATTRACTION ENTER;A01;TG01"));
        assertEquals("EXITED A01: luis@correo.com", run("ATTRACTION EXIT;A01;luis@correo.com"));
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Dragon Rojo"));
        assertTrue(status.contains("CAPACITY: 20"));
        assertTrue(status.contains("INSIDE: 2"));
        assertTrue(status.contains("AVAILABLE: 18"));
        assertTrue(status.contains("ana@correo.com"));
        assertTrue(status.contains("pedro@correo.com"));
        assertTrue(!status.contains("luis@correo.com"));
        assertTrue(run("ATTRACTION EXIT;A01;luis@correo.com").startsWith("ERROR"));
    }

    @Test
    void statusOfEmptyAttractionAndErrors() {
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        String status = run("ATTRACTION STATUS;A02");
        assertTrue(status.contains("INSIDE: 0"));
        assertTrue(status.contains("AVAILABLE: 12"));
        assertTrue(status.contains("USERS: none"));
        assertTrue(run("ATTRACTION STATUS;A99").startsWith("ERROR"));
        assertTrue(run("ATTRACTION EXIT;A99;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("ATTRACTION EXIT;A02").startsWith("ERROR"));
    }

    @Test
    void helpListsAttractionCommands() {
        String help = run("HELP");
        assertTrue(help.contains("ATTRACTION EXIT;<AttractionIdentifier>;<Email>"));
        assertTrue(help.contains("ATTRACTION STATUS;<AttractionIdentifier>"));
        assertTrue(help.contains("ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>"));
        assertTrue(help.contains("ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>"));
        assertTrue(help.contains("ATTRACTION LIST;<AttractionIdentifier>"));
    }
}
