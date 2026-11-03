package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    @Test
    void reconstructsRelationshipsAndEmptiesAttractions() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, ParkFixture.LUIS,
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com", "SYSTEM PRICE;50",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-16",
                "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION ENTER;A01;T001");
        f.restart();
        Park park = f.park;

        Group g = park.findGroup("G01");
        assertEquals("Familia Garcia", g.getName());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com"),
                g.getMembers().stream().map(User::getEmail).toList());
        assertSame(park.findUser("ana@correo.com"), g.getMembers().get(0));

        IndividualTicket t = (IndividualTicket) park.findTicket("T001");
        assertSame(park.findUser("ana@correo.com"), t.getUser());
        assertEquals("50.00", t.getPrice().toPlainString());

        GroupTicket gt = (GroupTicket) park.findTicket("TG01");
        assertSame(g, gt.getGroup());
        assertEquals("135.00", gt.getPrice().toPlainString());
        assertEquals(3, gt.getComposition().size());
        assertSame(park.findUser("luis@correo.com"), gt.getComposition().get(2).getUser());

        Attraction a = park.findAttraction("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertTrue(park.isGroupLocked(g));
    }

    @Test
    void historicalTicketsOfDeletedUsersAndGroupsSurviveRestart() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SYSTEM DATE;2027-04-16", "USER DELETE;ana@correo.com;Clave1!", "GROUP DELETE;G01");
        f.restart();
        IndividualTicket t = (IndividualTicket) f.park.findTicket("T001");
        assertEquals("12345678Z", t.getUser().getDni());
        assertEquals("ana@correo.com", t.getUser().getEmail());
        assertNull(f.park.findUser("ana@correo.com"));
        GroupTicket gt = (GroupTicket) f.park.findTicket("TG01");
        assertNull(gt.getGroup());
        assertEquals("40.00", gt.getPrice().toPlainString());
        assertEquals("TG01:12345678Z", gt.getComposition().get(0).getId());
        // the detached historical holder does not count as an active user
        assertEquals("USER CREATED", f.run(ParkFixture.ANA));
    }

    @Test
    void writesDocumentedFileFormats() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "TICKET CREATE;T001;ana@correo.com;2027-04-16",
                "ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertEquals(List.of("G01;Familia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        assertEquals(List.of("A01;Dragon Rojo;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        assertEquals(List.of(
                "G;TG01;2027-04-15;G01",
                "C;TG01;TG01:12345678Z;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "I;T001;2027-04-16;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez"),
                Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void skipsMalformedRecords() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        Files.writeString(dir.resolve("groups.txt"), "G01;Familia;12345678Z,99999999R\nbroken\n");
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon;20\nA02;Bad;zero\nA03;Bad;0\nshort\n");
        Files.writeString(dir.resolve("tickets.txt"), String.join("\n",
                "I;T001;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "I;T002;not-a-date;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "I;short",
                "G;short",
                "C;NOPE;X;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "X;unknown", ""));
        Park park = TestParks.start(dir);
        assertEquals(1, park.findGroup("G01").getMembers().size());
        assertEquals(1, park.getAttractions().size());
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void failedSaveRollsBackParkCommands() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-16", "GROUPTICKET CREATE;TG01;G01;2027-04-16",
                "ATTRACTION CREATE;A01;Dragon;5", "GROUP CREATE;G02;Old", "GROUP ADD;G02;pedro@correo.com",
                "GROUPTICKET CREATE;TG02;G02;2027-04-15", "SYSTEM DATE;2027-04-16");
        Park park = f.park;
        park.start(new ParkStorage(dir) {
            @Override
            public void saveAll(java.util.Collection<User> u, java.util.Collection<Group> g,
                                java.util.Collection<Ticket> t, java.util.Collection<Attraction> a) throws IOException {
                throw new IOException("disk full");
            }
        }, TestParks.TODAY.plusDays(1));
        CommandProcessor p = new CommandProcessor(park, new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        p.process("GROUP CREATE;G03;Nuevo");
        p.process("GROUP REMOVE;G02;pedro@correo.com");
        p.process("GROUP ADD;G02;ana@correo.com");
        p.process("GROUP DELETE;G02");
        p.process("TICKET CREATE;T002;ana@correo.com;2027-04-17");
        p.process("GROUPTICKET CREATE;TG03;G02;2027-04-17");
        p.process("ATTRACTION CREATE;A02;Noria;5");
        p.process("SYSTEM DATE;2027-04-15");
        p.process("TICKET DELETE;T001");
        p.process("GROUPTICKET DELETE;TG01");
        p.process("SYSTEM DATE;2027-04-17");
        p.process("USER DELETE;pedro@correo.com;Otra2#");
        assertNull(park.findGroup("G03"));
        assertEquals(List.of("pedro@correo.com"), park.findGroup("G02").getMembers().stream().map(User::getEmail).toList());
        assertSame(park.findGroup("G02"), ((GroupTicket) park.findTicket("TG02")).getGroup());
        assertNull(park.findTicket("T002"));
        assertNull(park.findTicket("TG03"));
        assertNull(park.findAttraction("A02"));
        assertTrue(park.findTicket("T001") != null && park.findTicket("TG01") != null);
        assertTrue(park.findUser("pedro@correo.com") != null);
    }
}
