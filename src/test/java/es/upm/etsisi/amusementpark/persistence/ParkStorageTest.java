package es.upm.etsisi.amusementpark.persistence;

import static es.upm.etsisi.amusementpark.park.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.park.ParkFixture;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.user.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkStorageTest {

    @TempDir
    Path dir;

    @Test
    void parkStateIsReconstructedAfterRestart() throws Exception {
        Park park = ParkFixture.reset(dir);
        park.createUser(ParkFixture.ana());
        park.createUser(ParkFixture.pedro());
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroup("G02", "Old");
        park.addToGroup("G02", "pedro@correo.com");
        park.createGroupTicket("TG02", "G02", TODAY);
        park.setBasePrice(new BigDecimal("45.50"));
        park.createTicket("T001", "ana@correo.com", TODAY);
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(1));
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.enter("A01", "T001");
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G02");

        park = ParkFixture.reset(dir);
        User ana = park.getUsers().get(0);
        Group g = park.findGroup("G01").orElseThrow();
        assertEquals("Familia Garcia", g.getName());
        assertEquals(2, g.getMembers().size());
        assertSame(ana, g.getMembers().get(0));
        IndividualTicket t = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertSame(ana, t.getUser());
        assertEquals(new BigDecimal("45.50"), t.getPrice());
        GroupTicket tg = (GroupTicket) park.findTicket("TG01").orElseThrow();
        assertSame(g, tg.getGroup());
        assertEquals(TODAY.plusDays(1), tg.getDate());
        assertEquals(new BigDecimal("91.00"), tg.getPrice());
        assertSame(ana, tg.getIndividualTickets().get(0).getUser());
        assertEquals("TG01:12345678Z", tg.getIndividualTickets().get(0).getId());
        GroupTicket old = (GroupTicket) park.findTicket("TG02").orElseThrow();
        assertNull(old.getGroup());
        assertEquals(1, old.getIndividualTickets().size());
        Attraction a = park.requireAttraction("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertTrue(park.isLocked(g));
    }

    @Test
    void historicalTicketsOfDeletedUserSurviveRestart() throws Exception {
        Park park = ParkFixture.reset(dir);
        park.createUser(ParkFixture.ana());
        park.createTicket("T1", "ana@correo.com", TODAY);
        park.createTicket("T2", "ana@correo.com", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");

        park = ParkFixture.reset(dir);
        assertTrue(park.getUsers().isEmpty());
        IndividualTicket t1 = (IndividualTicket) park.findTicket("T1").orElseThrow();
        IndividualTicket t2 = (IndividualTicket) park.findTicket("T2").orElseThrow();
        assertEquals("12345678Z", t1.getUser().getDni());
        assertEquals("ana@correo.com", t1.getUser().getEmail());
        assertSame(t1.getUser(), t2.getUser());
        assertEquals(new BigDecimal("40.00"), t1.getPrice());
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        Files.writeString(dir.resolve("groups.txt"), "bad\nG01;Fam;12345678Z,99999999R\n\n");
        Files.writeString(dir.resolve("attractions.txt"), "A01;X;notanumber\nA02;Y\nA03;Z;0\nA04;Ok;4\n\n");
        Files.writeString(dir.resolve("tickets.txt"), "WHAT;x\nINDIVIDUAL;T1;bad-date;40;0;1;a;b;c\n"
                + "GROUP;TG;2027-04-15\n\nINDIVIDUAL;T2;2027-04-15;40.00;0;12345678Z;Ana;Garcia Lopez;ana@correo.com\n");
        ByteArrayOutputStream warnings = new ByteArrayOutputStream();
        ParkStorage storage = new ParkStorage(dir, new PrintStream(warnings, true, StandardCharsets.UTF_8));
        ParkData data = storage.load();
        assertEquals(1, data.groups().size());
        assertEquals(1, data.groups().get(0).getMembers().size());
        assertEquals(List.of("A04"), data.attractions().stream().map(Attraction::getId).toList());
        assertEquals(1, data.tickets().size());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("WARNING"));
        assertEquals(dir, storage.getDirectory());
    }

    @Test
    void failedSaveRestoresEveryFile() throws Exception {
        Park park = ParkFixture.reset(dir);
        park.createUser(ParkFixture.ana());
        park.createGroup("G01", "Fam");
        String usersBefore = Files.readString(dir.resolve("users.txt"));
        String groupsBefore = Files.readString(dir.resolve("groups.txt"));
        Files.delete(dir.resolve("tickets.txt"));
        Files.createDirectories(dir.resolve("tickets.txt").resolve("blocker"));
        ParkStorage storage = new ParkStorage(dir, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> storage.save(List.of(), List.of(), List.of(), List.of()));
        assertEquals(usersBefore, Files.readString(dir.resolve("users.txt")));
        assertEquals(groupsBefore, Files.readString(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("tickets.txt.tmp")));
    }

    @Test
    void temporaryDirectoryLifecycle() throws Exception {
        Path tmp = dir.resolve("tmp");
        LocalDateTime start = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        Path first = TemporaryDataDirectory.create(tmp, start);
        Path second = TemporaryDataDirectory.create(tmp, start);
        assertEquals(tmp.resolve("20260826_190315"), first);
        assertEquals(tmp.resolve("20260826_190315_1"), second);
        Files.writeString(first.resolve("users.txt"), "x");
        Files.createDirectories(first.resolve("sub"));
        TemporaryDataDirectory.delete(first);
        assertFalse(Files.exists(first));
        assertTrue(Files.exists(tmp));
        TemporaryDataDirectory.delete(second);
        assertFalse(Files.exists(tmp));
        TemporaryDataDirectory.delete(second);
    }
}
