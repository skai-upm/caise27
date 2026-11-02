package es.upm.etsisi.amusementpark.persistence;

import static es.upm.etsisi.amusementpark.TestSupport.ANA;
import static es.upm.etsisi.amusementpark.TestSupport.PEDRO;
import static es.upm.etsisi.amusementpark.TestSupport.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.park.ParkData;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileParkRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void stateIsReconstructedAfterRestart() throws Exception {
        run(TestSupport.start(dir), ANA, PEDRO, "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUP CREATE;G02;Old", "GROUP ADD;G02;pedro@correo.com",
                "GROUPTICKET CREATE;TG02;G02;2027-04-15", "SYSTEM PRICE;45.50",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-16",
                "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATE;A04;Terror;5;SPECIAL",
                "SPEEDPASS CREATE;SP;T001;10", "SPECIALPASS CREATE;PE;T001", "ATTRACTION ENTER;A01;T001;SPEED",
                "ATTRACTION ENTER;A04;T001", "SYSTEM DATE;2027-04-16", "GROUP DELETE;G02");
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Clave1!"));

        TestSupport.start(dir);
        Park park = Park.getInstance();
        User ana = park.users().get(0);
        assertTrue(TestSupport.HASHER.matches("Clave1!", ana.getPasswordHash()));
        Group g = park.findGroup("G01").orElseThrow();
        assertSame(ana, g.getMembers().get(0));
        IndividualTicket t = (IndividualTicket) park.findTicket("T001").orElseThrow();
        assertSame(ana, t.getUser());
        assertEquals(new BigDecimal("45.50"), t.getPrice());
        GroupTicket tg = (GroupTicket) park.findTicket("TG01").orElseThrow();
        assertSame(g, tg.getGroup());
        assertEquals(new BigDecimal("91.00"), tg.getPrice());
        assertNull(((GroupTicket) park.findTicket("TG02").orElseThrow()).getGroup());
        Attraction<?> special = park.findAttraction("A04").orElseThrow();
        assertEquals(AttractionType.SPECIAL, special.getType());
        assertTrue(special.getOccupants().isEmpty());
        assertEquals(11, park.passes().size());
        Pass consumed = park.findPass("SP:1").orElseThrow();
        assertTrue(consumed.isConsumed());
        assertSame(t, consumed.getTicket());
        assertEquals(new BigDecimal("9.10"), consumed.getPrice());
        assertFalse(park.findPass("SP:2").orElseThrow().isConsumed());
        assertTrue(park.findPass("PE").orElseThrow() instanceof SpecialPass sp && sp.isConsumed());
    }

    @Test
    void historicalTicketsOfDeletedUserSurviveRestart() throws Exception {
        run(TestSupport.start(dir), ANA, "TICKET CREATE;T1;ana@correo.com;2027-04-15",
                "TICKET CREATE;T2;ana@correo.com;2027-04-15", "SYSTEM DATE;2027-04-16",
                "USER DELETE;ana@correo.com;Clave1!");
        TestSupport.start(dir);
        Park park = Park.getInstance();
        assertTrue(park.users().isEmpty());
        IndividualTicket t1 = (IndividualTicket) park.findTicket("T1").orElseThrow();
        assertEquals("12345678Z", t1.getUser().getDni());
        assertSame(t1.getUser(), ((IndividualTicket) park.findTicket("T2").orElseThrow()).getUser());
    }

    @Test
    void legacyAndMalformedLines() throws Exception {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nbroken\n\n");
        Files.writeString(dir.resolve("groups.txt"), "bad\nG01;Fam;12345678Z,99999999R\n\nG02;Empty;\n");
        Files.writeString(dir.resolve("attractions.txt"), "A01;X;notanumber\nA02;Y\nA03;Z;0\nA04;Old;4\n"
                + "A05;New;4;GROUP\nA06;Bad;4;VIP\n\n");
        Files.writeString(dir.resolve("tickets.txt"), "WHAT;x\nINDIVIDUAL;T1;bad-date;40;0;1;a;b;c\n"
                + "GROUP;TG;2027-04-15\n\nINDIVIDUAL;T2;2027-04-15;40.00;0;12345678Z;Ana;Garcia Lopez;ana@correo.com\n");
        Files.writeString(dir.resolve("passes.txt"), "SPEED;S:1;T2;8.00;false\nSPEED;S:2;T9;8.00;false\n"
                + "OTHER;S:3;T2;8.00;false\nSPEED;S:4\n\n");
        ByteArrayOutputStream warnings = new ByteArrayOutputStream();
        FileParkRepository repository = new FileParkRepository(dir,
                new PrintStream(warnings, true, StandardCharsets.UTF_8));
        ParkData data = repository.load();
        assertEquals(1, data.users().size());
        assertEquals(2, data.groups().size());
        assertEquals(1, data.groups().get(0).getMembers().size());
        assertEquals(List.of("A04", "A05"), data.attractions().stream().map(Attraction::getId).toList());
        assertEquals(AttractionType.GENERAL, data.attractions().get(0).getType());
        assertEquals(1, data.tickets().size());
        assertEquals(List.of("S:1"), data.passes().stream().map(Pass::getId).toList());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("WARNING"));
        assertEquals(dir, repository.getDirectory());
        // legacy plaintext passwords are not migrated and simply fail authentication
        TestSupport.start(dir);
        assertTrue(TestSupport.run(TestSupport.start(dir), "USER DELETE;ana@correo.com;Clave1!").get(0)
                .startsWith("ERROR: incorrect password"));
    }

    @Test
    void unreadableDataIsReported() throws Exception {
        Files.createDirectories(dir.resolve("users.txt"));
        assertThrows(StorageException.class, () -> TestSupport.fileRepository(dir).load());
    }

    @Test
    void failedSaveRestoresEveryFile() throws Exception {
        run(TestSupport.start(dir), ANA, "GROUP CREATE;G01;Fam");
        String usersBefore = Files.readString(dir.resolve("users.txt"));
        String groupsBefore = Files.readString(dir.resolve("groups.txt"));
        Files.delete(dir.resolve("tickets.txt"));
        Files.createDirectories(dir.resolve("tickets.txt").resolve("blocker"));
        FileParkRepository repository = TestSupport.fileRepository(dir);
        assertThrows(StorageException.class,
                () -> repository.save(new ParkData(List.of(), List.of(), List.of(), List.of(), List.of())));
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
