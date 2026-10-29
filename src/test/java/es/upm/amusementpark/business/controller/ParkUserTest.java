package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.ParkException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkUserTest extends ParkTestSupport {

    private User ana;

    @BeforeEach
    void users() throws Exception {
        ana = createAna();
        createPedro();
    }

    @Test
    void createAndUpdatePersist() throws Exception {
        park.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        restart();
        assertNotNull(park.findUser("ana@correo.com"));
        assertTrue(park.findUser("ana@correo.com").getPhone().equals("699999999"));
    }

    @Test
    void deleteUserWithoutTicketsPersists() throws Exception {
        park.deleteUser("pedro@correo.com", "Otra2#");
        assertNull(park.findUser("pedro@correo.com"));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("pedro@correo.com"));
        restart();
        assertNull(park.findUser("pedro@correo.com"));
    }

    @Test
    void unknownEmailOrWrongPassword() {
        assertThrows(ParkException.class, () -> park.deleteUser("nadie@correo.com", "Clave1!"));
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Mala1!"));
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void activeIndividualTicketBlocksDeletion() throws Exception {
        park.createTicket("T001", "ana@correo.com", TOMORROW_TEXT);
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        park.deleteTicket("T001");
        park.deleteUser("ana@correo.com", "Clave1!");
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void ticketForTodayKeepsBlocking() throws Exception {
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void activeGroupTicketBlocksDeletion() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void memberOfLockedGroupCannotBeDeleted() throws Exception {
        // G01 buys TG01 for today with Pedro only; Ana joins while TG01 is historical (date moved
        // forward), then the date is moved back so TG01 is active again and locks G01.
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);
        park.addToGroup("G01", "ana@correo.com");
        park.setCurrentDate(TODAY_TEXT);

        assertTrue(park.ticketsOf(ana).isEmpty());
        assertTrue(park.isLocked(park.findGroup("G01")));
        assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertNotNull(park.findUser("ana@correo.com"));
        assertTrue(park.findGroup("G01").hasMember(ana));
    }

    @Test
    void historicalTicketsDoNotBlockAndAreKept() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);

        park.deleteUser("ana@correo.com", "Clave1!");

        assertNull(park.findUser("ana@correo.com"));
        assertTrue(park.findGroup("G01").isEmpty());
        assertNotNull(park.findTicket("T001"));
        assertNotNull(park.findTicket("TG01:12345678Z"));
        assertFalse(park.isActiveUser(ana));
    }

    @Test
    void deletedUserLeavesAttractions() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.enterAttraction("A01", "T001");
        park.setCurrentDate(TOMORROW_TEXT);
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }
}
