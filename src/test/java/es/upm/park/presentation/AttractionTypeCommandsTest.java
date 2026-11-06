package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.controller.TestUsers;
import es.upm.park.business.model.AttractionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionTypeCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com", "pedro@correo.com"));
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL");
        run("ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP");
        run("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private AttractionType type(String id) {
        return ParkController.getInstance().findAttraction(id).orElseThrow().getType();
    }

    @Test
    void typedAndDefaultCreation() {
        assertEquals(AttractionType.GENERAL, type("A01"));
        assertEquals(AttractionType.INDIVIDUAL, type("A02"));
        assertEquals(AttractionType.GROUP, type("A03"));
        assertEquals(AttractionType.SPECIAL, type("A04"));
        assertTrue(run("ATTRACTION CREATE;A05;X;5;VIP").startsWith("ERROR"));
        assertTrue(run("ATTRACTION CREATE;A05;X;5;GENERAL;EXTRA").startsWith("ERROR"));
        assertTrue(ParkController.getInstance().findAttraction("A05").isEmpty());
    }

    @Test
    void entryFollowsTypeRestrictions() {
        assertTrue(run("ATTRACTION ENTER;A02;TG01").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A03;T001").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A04;T001").startsWith("ERROR"), "no Special Pass");
        assertEquals("ENTERED A02: ana@correo.com", run("ATTRACTION ENTER;A02;T001"));
        assertEquals("ENTERED A03: pedro@correo.com", run("ATTRACTION ENTER;A03;TG01"));
        run("SPECIALPASS CREATE;PE01;T001");
        assertEquals("ENTERED A04: ana@correo.com (CONSUMED: PE01)", run("ATTRACTION ENTER;A04;T001"));
    }

    @Test
    void statusShowsTypeAndHelpListsTypedSyntax() {
        assertTrue(run("ATTRACTION STATUS;A04").contains("TYPE: SPECIAL"));
        assertTrue(run("HELP").contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>"));
    }
}
