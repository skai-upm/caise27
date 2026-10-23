package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.controller.Park;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @TempDir
    Path dir;

    private static void runAll(Park park, String... lines) {
        CommandProcessor processor = new CommandProcessor(park);
        for (String line : lines) {
            String output = processor.process(line).output();
            assertTrue(!output.startsWith("ERROR"), line + " -> " + output);
        }
    }

    @Test
    void fullParkStateRoundTrip() {
        runAll(TestParks.open(dir, DAY),
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;laura@correo.com",
                "GROUP CREATE;G02;Pareja",
                "GROUP ADD;G02;laura@correo.com",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "GROUPTICKET CREATE;TG02;G02;2027-04-15",
                "SYSTEM PRICE;45.50",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION ENTER;A01;TG01",
                "SYSTEM DATE;2027-04-16",
                "GROUP DELETE;G02",
                "GROUP REMOVE;G01;laura@correo.com",
                "USER DELETE;laura@correo.com;Laura3$");

        Park park = TestParks.open(dir, LocalDate.of(2027, 4, 16));
        assertEquals(2, park.getUsers().size());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"),
                park.findGroup("G01").getMembers().stream().map(u -> u.getEmail()).toList());
        assertNull(park.findGroup("G02"));

        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertSame(park.findGroup("G01"), tg01.getGroup());
        assertEquals(3, tg01.getComposition().size());
        assertEquals(new BigDecimal("108.00"), tg01.getPrice());
        assertEquals("laura@correo.com", tg01.getComposition().get(2).getUser().getEmail());

        GroupTicket tg02 = (GroupTicket) park.findTicket("TG02");
        assertNull(tg02.getGroup());
        assertEquals("11111111H", tg02.getComposition().get(0).getUser().getDni());
        assertSame(tg01.getComposition().get(2).getUser(), tg02.getComposition().get(0).getUser(),
                "one rebuilt former user per DNI");

        IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
        assertSame(park.findUserByEmail("ana@correo.com"), t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());

        Attraction<?> a01 = park.findAttraction("A01");
        assertTrue(a01.getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void writeAllChangesNoFileWhenOneWriteFails() throws IOException {
        Path ok = dir.resolve("users.txt");
        Files.writeString(ok, "old\n");
        Path locked = Files.createDirectory(dir.resolve("locked"));
        Map<Path, List<String>> contents = new LinkedHashMap<>();
        contents.put(ok, List.of("new"));
        contents.put(locked.resolve("groups.txt"), List.of("new"));
        assertTrue(locked.toFile().setWritable(false));
        try {
            assertThrows(IOException.class, () -> TextFiles.writeAll(contents));
        } finally {
            assertTrue(locked.toFile().setWritable(true));
        }
        assertEquals(List.of("old"), Files.readAllLines(ok));
        try (var stream = Files.list(dir)) {
            assertEquals(2, stream.count(), "no temporary files left behind");
        }
    }

    @Test
    void transactionWritesAllFilesTogether() throws IOException {
        FileDataStore store = new FileDataStore();
        PrintStream quiet = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        FileGroupRepository groups = new FileGroupRepository(dir.resolve("groups.txt"), store, quiet);
        FileAttractionRepository attractions = new FileAttractionRepository(dir.resolve("attractions.txt"), store,
                quiet);
        store.inTransaction(() -> {
            groups.saveAll(List.of(new Group("G01", "Familia")));
            assertTrue(!Files.exists(dir.resolve("groups.txt")), "staged until the transaction ends");
            attractions.saveAll(List.of(new GeneralAttraction("A01", "Dragon Rojo", 20)));
        });
        assertEquals(List.of("G01;Familia"), Files.readAllLines(dir.resolve("groups.txt")));
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));

        assertThrows(IOException.class, () -> store.inTransaction(() -> {
            groups.saveAll(List.of());
            throw new IOException("boom");
        }));
        assertEquals(List.of("G01;Familia"), Files.readAllLines(dir.resolve("groups.txt")));
    }
}
