package es.upm.etsisi.park.persistence.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.attraction.GeneralAttraction;
import es.upm.etsisi.park.business.model.attraction.SpecialAttraction;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;

class TextParkRepositoriesTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();
    private final PrintStream warningStream = new PrintStream(warnings, true, StandardCharsets.UTF_8);

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
            "612345678", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    @Test
    void groupsRoundTrip() throws Exception {
        TextGroupRepository repo = new TextGroupRepository(dir.resolve("groups.txt"), warningStream);
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        repo.save(List.of(g, new Group("G02", "Vacio")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"),
                Files.readAllLines(dir.resolve("groups.txt")));
        List<Group> loaded = repo.load(Map.of("12345678Z", ana, "87654321X", pedro));
        assertEquals(List.of(ana, pedro), loaded.get(0).getMembers());
        assertTrue(loaded.get(1).isEmpty());
    }

    @Test
    void attractionsRoundTripAndSkipMalformed() throws Exception {
        Path file = dir.resolve("attractions.txt");
        TextAttractionRepository repo = new TextAttractionRepository(file, warningStream);
        repo.save(List.of(new GeneralAttraction("A01", "Dragon Rojo", 20), new SpecialAttraction("A04", "Terror", 15)));
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL", "A04;Terror;15;SPECIAL"), Files.readAllLines(file));
        Files.writeString(file, "A02;Torre;abc\nbroken\nA03;Legacy;12\nA05;X;3;VIP\n",
                java.nio.file.StandardOpenOption.APPEND);
        List<Attraction<?>> loaded = repo.load();
        assertEquals(3, loaded.size());
        assertEquals(20, loaded.get(0).getCapacity());
        assertEquals(AttractionType.SPECIAL, loaded.get(1).getType());
        assertEquals(AttractionType.GENERAL, loaded.get(2).getType());
        String text = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("line 3"));
        assertTrue(text.contains("line 4"));
        assertTrue(text.contains("line 6"));
    }

    @Test
    void ticketsRoundTripWithFormerUserAndDetachedGroup() throws Exception {
        Path file = dir.resolve("tickets.txt");
        TextTicketRepository repo = new TextTicketRepository(file, warningStream);
        LocalDate day = LocalDate.of(2027, 4, 15);
        Group g = new Group("G01", "Familia");
        IndividualTicket t1 = new IndividualTicket("T001", day, ana, new BigDecimal("40"));
        GroupTicket tg = new GroupTicket("TG01", day, g, List.of(
                new IndividualTicket("TG01:12345678Z", day, ana, new BigDecimal("40"), 0),
                new IndividualTicket("TG01:87654321X", day, pedro, new BigDecimal("40"), 0)));
        GroupTicket orphan = new GroupTicket("TG02", day, null, List.of(
                new IndividualTicket("TG02:87654321X", day, pedro, new BigDecimal("45.50"), 0)));
        repo.save(List.of(t1, tg, orphan), List.of(ana));

        List<Ticket> loaded = repo.load(Map.of("12345678Z", ana), Map.of("G01", g));
        assertEquals(3, loaded.size());
        assertSame(ana, ((IndividualTicket) loaded.get(0)).getUser());
        GroupTicket loadedTg = (GroupTicket) loaded.get(1);
        assertSame(g, loadedTg.getGroup());
        User formerPedro = loadedTg.getMembers().get(1).getUser();
        assertEquals("pedro@correo.com", formerPedro.getEmail());
        GroupTicket loadedOrphan = (GroupTicket) loaded.get(2);
        assertNull(loadedOrphan.getGroup());
        assertSame(formerPedro, loadedOrphan.getMembers().get(0).getUser());
        assertEquals(new BigDecimal("45.50"), loadedOrphan.getPrice());
    }

    @Test
    void ticketsSkipMalformedLines() throws Exception {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of(
                "INDIVIDUAL;T001;2027-04-15;40.00;0;A;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "INDIVIDUAL;T002;not-a-date;40.00;0;A;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "MEMBER;TG99;TG99:1;2027-04-15;40.00;0;A;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "SOMETHING;else",
                "GROUP;TG03;2027-04-15;"));
        List<Ticket> loaded = new TextTicketRepository(file, warningStream).load(Map.of("12345678Z", ana), Map.of());
        assertEquals(1, loaded.size());
        String text = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("line 2"));
        assertTrue(text.contains("line 3"));
        assertTrue(text.contains("line 4"));
        assertTrue(text.contains("TG03"));
    }
}
