package es.upm.park.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerStorageTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private final List<ParkState> saved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.of("ana@correo.com", "pedro@correo.com"));
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
        park.reset();
    }

    @Test
    void everyMutationSavesAndRejectionsDoNot() {
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", TODAY.plusDays(1));
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(2));
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.deleteTicket("T001");
        park.deleteGroupTicket("TG01");
        park.removeUserFromGroup("G01", "ana@correo.com");
        park.deleteGroup("G01");
        assertEquals(9, saved.size());
        assertThrows(ParkException.class, () -> park.createGroup("", "x"));
        assertEquals(9, saved.size());
    }

    @Test
    void snapshotExcludesComposedTicketsAndRestoreRebuildsThem() {
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        ParkState state = park.snapshot();
        assertEquals(List.of("TG01", "T001"), state.tickets().stream().map(t -> t.getId()).toList());
        park.reset();
        park.setCurrentDate(TODAY);
        park.restore(state);
        assertTrue(park.findTicket("TG01:DNI1").isPresent());
        assertTrue(park.findTicket("TG01:DNI2").isPresent());
        assertTrue(park.isGroupLocked("G01"), "restored group ticket still references its group");
    }
}
