package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.App;
import es.upm.amusementpark.park.GroupTicket;
import es.upm.amusementpark.park.IndividualTicket;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.park.UserDirectory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final UserDirectory USERS = new UserDirectory() {
        @Override
        public boolean exists(String email) {
            return Set.of("ana@correo.com", "pedro@correo.com").contains(email);
        }

        @Override
        public String dniOf(String email) {
            return email.startsWith("ana") ? "12345678Z" : "87654321X";
        }
    };

    @TempDir
    Path dir;
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(USERS);
    }

    private void restart(ParkStorage storage, List<String> expectedWarnings) throws IOException {
        storage.save(park);
        park.reset();
        assertEquals(expectedWarnings, storage.load(park));
    }

    @Test
    void groupsTicketsAndAttractionsRoundTrip() throws IOException {
        ParkStorage storage = new ParkStorage(dir);
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentPrice(new BigDecimal("45.50"));
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(2));
        park.enterAttraction("A01", "T001");
        restart(storage, List.of());

        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
        assertEquals(20, park.findAttraction("A01").getCapacity());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), List.copyOf(park.findGroup("G01").getMembers()));
        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertEquals("ana@correo.com", t001.getUserEmail());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        assertNull(t001.getContainingGroupTicket());
        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertEquals(TODAY.plusDays(2), tg01.getDate());
        assertEquals(new BigDecimal("91.00"), tg01.getPrice());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                tg01.getComponents().stream().map(IndividualTicket::getId).toList());
        assertEquals(tg01, ((IndividualTicket) park.findTicket("TG01:12345678Z")).getContainingGroupTicket());
        assertEquals(park.findGroup("G01"), tg01.getGroup());
        assertTrue(park.isGroupLocked("G01"));
    }

    @Test
    void deletedGroupIsNotRelinkedEvenIfIdReused() throws IOException {
        ParkStorage storage = new ParkStorage(dir);
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G01");
        park.createGroup("G01", "Otra");
        restart(storage, List.of());
        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertNull(tg01.getGroup());
        assertEquals("G01", tg01.getGroupId());
        assertEquals(TODAY, tg01.getDate());
        assertEquals(new BigDecimal("40.00"), tg01.getPrice());
        assertEquals(1, tg01.getComponents().size());
        assertNotNull(park.findGroup("G01"));
    }

    @Test
    void missingFilesMeanEmptyState() throws IOException {
        assertEquals(List.of(), new ParkStorage(dir.resolve("none")).load(park));
        assertTrue(park.getTickets().isEmpty());
        assertTrue(park.getGroups().isEmpty());
        assertTrue(park.getAttractions().isEmpty());
    }

    @Test
    void corruptedLinesAreReportedAndSkipped() throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), "broken line\nA02;Torre del Cielo;12\nA03;Noria;x\n");
        Files.writeString(dir.resolve("tickets.txt"), "GROUP;TG9;2027-04-15;G9;true;3\nCOMPONENT;a\n");
        List<String> warnings = new ParkStorage(dir).load(park);
        assertEquals(4, warnings.size(), warnings.toString());
        assertEquals(12, park.findAttraction("A02").getCapacity());
        assertNull(park.findAttraction("A03"));
        assertTrue(park.getTickets().isEmpty());
    }

    private static String runApp(Path base, String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new BufferedReader(new StringReader(stdin)), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                base);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static boolean tmpIsEmpty(Path base) throws IOException {
        Path tmp = base.resolve("tmp");
        if (!Files.exists(tmp)) {
            return true;
        }
        try (Stream<Path> entries = Files.list(tmp)) {
            return entries.findAny().isEmpty();
        }
    }

    @Test
    void normalModePersistsAcrossRunsWithoutExit() throws IOException {
        Path cmd = dir.resolve("input.txt");
        Files.writeString(cmd, "GROUP CREATE;G02;Amigos\nATTRACTION CREATE;A01;Dragon Rojo;20\n");
        runApp(dir, "", cmd.toString());
        String out = runApp(dir, "GROUP CREATE;G02;Otra\nATTRACTION LIST;A01\nEXIT\n");
        assertTrue(out.contains("ERROR: Group G02 already exists"));
        assertTrue(out.contains("ATTRACTION A01 USERS: (none)"));
        assertTrue(Files.exists(dir.resolve("data").resolve("groups.txt")));
    }

    @Test
    void flyModeIsIsolatedAndCleanedUp() throws IOException {
        runApp(dir, "GROUP CREATE;G01;Normal\nEXIT\n");
        Path cmd = dir.resolve("fly.txt");
        Files.writeString(cmd, "GROUP CREATE;G01;Fly\nGROUP CREATE;G09;Fly\nEXIT\n");
        String fly = runApp(dir, "", "--fly", cmd.toString());
        assertFalse(fly.contains("ERROR"), fly);
        assertTrue(tmpIsEmpty(dir));
        String normal = runApp(dir, "GROUP CREATE;G09;Again\nEXIT\n");
        assertTrue(normal.contains("GROUP CREATED"));
        assertFalse(Files.readString(dir.resolve("data").resolve("groups.txt")).contains("Fly"));
    }

    @Test
    void flyModeInteractiveAndWithoutExitCleanUp() throws IOException {
        runApp(dir, "GROUP CREATE;G01;X\n", "--fly");
        assertTrue(tmpIsEmpty(dir));
        Path cmd = dir.resolve("noexit.txt");
        Files.writeString(cmd, "GROUP CREATE;G01;X\n");
        runApp(dir, "", "--fly", cmd.toString());
        assertTrue(tmpIsEmpty(dir));
        assertFalse(Files.exists(dir.resolve("data")));
    }
}
