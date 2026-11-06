package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.controller.ParkState;
import es.upm.park.business.controller.ParkStorage;
import es.upm.park.business.controller.TestUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassEntryCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();
    private final ParkController park = ParkController.getInstance();
    private final List<ParkState> saved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        park.setUserDirectory(TestUsers.of("ana@correo.com", "pedro@correo.com", "luis@correo.com"));
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("ATTRACTION CREATE;A03;Rafting;24;GROUP");
        run("ATTRACTION CREATE;A04;Terror;15;SPECIAL");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUP CREATE;G01;Amigos");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;luis@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        park.setStorage(new ParkStorage() {
            @Override
            public ParkState load() {
                return ParkState.empty();
            }

            @Override
            public void save(ParkState state) {
                saved.add(state);
            }
        });
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private boolean consumed(String passId) {
        return park.findPass(passId).orElseThrow().isConsumed();
    }

    @Test
    void individualSpeedEntry() {
        run("SPEEDPASS CREATE;SP01;T001;1");
        run("SPEEDPASS CREATE;SP02;T001;1");
        assertEquals("ENTERED A01: ana@correo.com (CONSUMED: SP01:1)", run("ATTRACTION ENTER;A01;T001;SPEED"));
        assertTrue(consumed("SP01:1"));
        assertFalse(consumed("SP02:1"));
        assertTrue(saved.get(saved.size() - 1).passes().stream()
                .anyMatch(pass -> pass.getId().equals("SP01:1") && pass.isConsumed()), "consumption is saved");
    }

    @Test
    void groupSpeedEntryIsAllOrNothing() {
        run("SPEEDPASS CREATE;SP05;TG01:DNI2;1");
        assertTrue(run("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ERROR"));
        assertFalse(consumed("SP05:1"));
        assertTrue(park.findAttraction("A03").orElseThrow().getOccupantEmails().isEmpty());
        run("SPEEDPASS CREATE;SP06;TG01:DNI3;1");
        String out = run("ATTRACTION ENTER;A03;TG01;SPEED");
        assertTrue(out.startsWith("ENTERED A03: pedro@correo.com, luis@correo.com (CONSUMED: "), out);
        assertTrue(consumed("SP05:1") && consumed("SP06:1"));
    }

    @Test
    void specialEntryConsumesPassAutomatically() {
        run("SPECIALPASS CREATE;PE01;T001");
        assertTrue(run("ATTRACTION ENTER;A04;T001;SPEED").startsWith("ERROR"));
        assertFalse(consumed("PE01"));
        assertEquals("ENTERED A04: ana@correo.com (CONSUMED: PE01)", run("ATTRACTION ENTER;A04;T001"));
        run("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(run("ATTRACTION ENTER;A04;T001").startsWith("ERROR"));
        assertTrue(run("SPECIALPASS DELETE;PE01").startsWith("ERROR"), "consumed pass cannot be refunded");
    }

    @Test
    void unknownOptionAndHelp() {
        assertTrue(run("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR"));
        assertTrue(park.findAttraction("A01").orElseThrow().getOccupantEmails().isEmpty());
        assertTrue(run("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>;SPEED"));
    }
}
