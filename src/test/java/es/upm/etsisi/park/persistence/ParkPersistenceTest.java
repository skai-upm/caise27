package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Park;
import es.upm.etsisi.park.business.ParkTestSupport;
import es.upm.etsisi.park.business.Ticket;
import es.upm.etsisi.park.business.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

class ParkPersistenceTest extends ParkTestSupport {

    private void populate() {
        ana();
        pedro();
        groups.create("G01", "Familia Garcia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        groups.create("G02", "Vacio");
        tickets.create("T001", "ana@correo.com", TODAY_S);
        park.setBasePrice("45.50");
        tickets.createGroupTicket("TG01", "G01", TOMORROW_S);
        attractions.create("A01", "Dragon Rojo", "20", null);
        attractions.enter("A01", "T001", false).entered();
    }

    @Test
    void restartRestoresStateAndRelationships() {
        populate();
        Park p = restart();
        User ana = p.findUser("ana@correo.com");
        User pedro = p.findUser("pedro@correo.com");
        Group g = p.findGroup("G01");
        assertEquals(List.of(ana, pedro), g.getMembers());
        assertTrue(p.findGroup("G02").getMembers().isEmpty());

        IndividualTicket t = (IndividualTicket) p.findTicket("T001");
        assertSame(ana, t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());

        GroupTicket tg = (GroupTicket) p.findTicket("TG01");
        assertSame(g, tg.getGroup());
        assertEquals(List.of(ana, pedro), tg.getHolders());
        assertEquals(new BigDecimal("91.00"), tg.getPrice());
        assertEquals(p.getTickets().size(), 2);
        assertTrue(p.isGroupLocked(g));

        Attraction<?> a = p.findAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty(), "occupants are not persisted");
    }

    @Test
    void usersFileFormatKeepsColumnsWithHashedPassword() throws IOException {
        populate();
        List<String> lines = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals(2, lines.size());
        String[] ana = lines.get(0).split(";", -1);
        assertEquals(7, ana.length);
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com", String.join(";", List.of(ana).subList(0, 4)));
        assertTrue(ana[4].startsWith("$2a$"), "BCrypt hash stored");
        assertEquals("612345678;1234567890123456", ana[5] + ";" + ana[6]);
    }

    @Test
    void deletedUserHistoryIsReconstructed() {
        ana();
        tickets.create("T001", "ana@correo.com", TODAY_S);
        park.setToday(TOMORROW_S);
        users.delete("ana@correo.com", "Clave1!");
        Park p = restart();
        assertNull(p.findUser("ana@correo.com"));
        IndividualTicket t = (IndividualTicket) p.findTicket("T001");
        assertNotNull(t);
        assertEquals("12345678Z", t.getUser().getDni());
        assertEquals("Ana", t.getUser().getFirstName());
        assertEquals(TODAY, t.getDate());
        assertEquals(new BigDecimal("40.00"), t.getPrice());
    }

    @Test
    void deletedGroupHistoryIsReconstructed() {
        groupOf("G03", 3);
        tickets.createGroupTicket("TG03", "G03", TODAY_S);
        park.setToday(TOMORROW_S);
        groups.delete("G03");
        Park p = restart();
        GroupTicket tg = (GroupTicket) p.findTicket("TG03");
        assertNull(tg.getGroup());
        assertEquals(3, tg.getComposition().size());
        assertEquals(new BigDecimal("108.00"), tg.getPrice());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Files.write(dir.resolve("users.txt"), List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        Files.write(dir.resolve("groups.txt"), List.of("G01;Familia;12345678Z;99999999R", "bad"));
        Files.write(dir.resolve("tickets.txt"), List.of("X;nonsense", "G;TG9;" + TODAY_S + ";G01",
                "I;T1;" + TODAY_S + ";40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez"));
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;5", "A02;broken"));
        Park p = restart();
        assertEquals(1, p.findGroup("G01").getMembers().size());
        assertNull(p.findTicket("TG9"), "group ticket without composition skipped");
        assertNotNull(p.findTicket("T1"));
        assertNotNull(p.findAttraction("A01"));
        assertNull(p.findAttraction("A02"));
    }

    @Test
    void noSerializationUsed() {
        for (Class<?> c : List.<Class<?>>of(User.class, Group.class, Ticket.class, Attraction.class)) {
            assertTrue(!java.io.Serializable.class.isAssignableFrom(c), c + " must not rely on serialization");
        }
    }
}
