package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParkPersistenceTest extends ParkTestSupport {

    @Test
    void reloadsWholeParkState() {
        for (int i = 1; i <= 3; i++) {
            user(i);
        }
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", email(1));
        park.addToGroup("G01", email(2));
        park.addToGroup("G01", email(3));
        park.createTicket("T001", email(1), "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-16");
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T002", email(2), TODAY.toString());
        park.enterAttraction("A01", "T002");

        reload();

        User u1 = park.getUserService().findByEmail(email(1));
        Group g = park.getGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(3, g.getMembers().size());
        assertSame(u1, g.getMembers().get(0));

        IndividualTicket t = (IndividualTicket) park.findTicket("T001");
        assertSame(u1, t.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());

        GroupTicket tg = (GroupTicket) park.findTicket("TG01");
        assertSame(g, tg.getGroup());
        assertEquals(new BigDecimal("108.00"), tg.getPrice());
        assertEquals(List.of("TG01:" + dni(1), "TG01:" + dni(2), "TG01:" + dni(3)),
                tg.getComposition().stream().map(Ticket::getId).toList());
        assertSame(u1, tg.getComposition().get(0).getUser());
        assertTrue(park.isLocked(g));

        Attraction a = park.getAttraction("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty(), "occupants are not persisted");
    }

    @Test
    void historicalRecordsSurviveDeletions() {
        user(1);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", email(1));
        park.createTicket("T001", email(1), "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate(LocalDate.of(2027, 4, 20));
        park.deleteGroup("G01");
        park.deleteUser(email(1), "Clave1!");

        reload();

        assertNull(park.getUserService().findByEmail(email(1)));
        IndividualTicket t = (IndividualTicket) park.findTicket("T001");
        assertNotNull(t);
        assertEquals(dni(1), t.getUser().getDni());
        assertEquals(email(1), t.getUser().getEmail());
        GroupTicket tg = (GroupTicket) park.findTicket("TG01");
        assertNull(tg.getGroup());
        assertEquals(new BigDecimal("40.00"), tg.getPrice());
        assertEquals(dni(1), tg.getComposition().get(0).getUser().getDni());
    }

    @Test
    void usersFileFormatUnchanged() throws Exception {
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }
}
