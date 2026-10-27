package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Persistence of the complete park state between executions, and the
 * temporary --fly mode.
 */
class PersistenceIntegrationTest {

    private static final String SETUP = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave3$;600000000;1111222233334444",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "SYSTEM DATE;2027-04-01",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION ENTER;A01;T001",
            "EXIT",
            "");

    @TempDir
    Path tempDir;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), tempDir.resolve("data"), tempDir.resolve("tmp"));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** File names and contents of a directory, for comparison. */
    private static String snapshot(Path dir) throws IOException {
        StringBuilder result = new StringBuilder();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                result.append(file.getFileName()).append('=').append(Files.readString(file)).append('\n');
            }
        }
        return result.toString();
    }

    private static ParkController park() {
        return ParkController.getInstance();
    }

    @Test
    void wholeParkStateSurvivesRestart() {
        String out = run(SETUP);
        assertFalse(out.contains("ERROR"), out);
        run("SYSTEM DATE;2027-04-15\n");

        User ana = park().getUsers().findByEmail("ana@correo.com").orElseThrow();
        Group g01 = park().getGroups().findById("G01").orElseThrow();
        assertEquals(2, g01.getMembers().size());
        assertTrue(g01.contains(ana));
        IndividualTicket t001 = (IndividualTicket) park().getTickets().findById("T001").orElseThrow();
        assertTrue(t001.getUser() == ana);
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park().getTickets().findById("TG01").orElseThrow();
        assertTrue(tg01.getGroup() == g01);
        assertEquals(2, tg01.getComposition().size());
        assertTrue(park().getTickets().exists("TG01:87654321X"));
        Attraction a01 = park().getAttractions().findById("A01").orElseThrow();
        assertEquals("Dragon Rojo", a01.getName());
        assertEquals(20, a01.getCapacity());
        assertTrue(a01.getOccupants().isEmpty());
    }

    @Test
    void locksAreReconstructedAfterRestart() {
        run(SETUP);
        String out = run("SYSTEM DATE;2027-04-15\nGROUP ADD;G01;luis@correo.com\nUSER DELETE;ana@correo.com;Clave1!\n");
        String[] lines = out.split(System.lineSeparator());
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[2].startsWith("ERROR"));
    }

    @Test
    void historicalTicketsOfDeletedUsersAndGroupsSurvive() {
        run(SETUP);
        String out = run("SYSTEM DATE;2027-04-20\nUSER DELETE;ana@correo.com;Clave1!\nGROUP DELETE;G01\n");
        assertFalse(out.contains("ERROR"), out);
        run("");
        IndividualTicket t001 = (IndividualTicket) park().getTickets().findById("T001").orElseThrow();
        assertEquals("12345678Z", t001.getUser().getDni());
        assertTrue(park().getUsers().findByEmail("ana@correo.com").isEmpty());
        GroupTicket tg01 = (GroupTicket) park().getTickets().findById("TG01").orElseThrow();
        assertNull(tg01.getGroup());
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());
        assertEquals(2, tg01.getComposition().size());
    }

    @Test
    void flyRunWithFileIsIsolatedAndLeavesNoData() throws IOException {
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        String dataBefore = snapshot(tempDir.resolve("data"));
        Path input = tempDir.resolve("input_fase2.txt");
        Files.writeString(input, SETUP);

        String out = run("", "--fly", input.toString());
        assertFalse(out.contains("ERROR"), out);
        assertTrue(out.startsWith("USER CREATED"));
        assertFalse(Files.exists(tempDir.resolve("tmp")));
        assertEquals(dataBefore, snapshot(tempDir.resolve("data")));
    }

    @Test
    void flyRunInteractiveUsesTemporaryDirectoryAndCleansUp() {
        String out = run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "EXIT\n", "--fly");
        assertTrue(out.startsWith("USER CREATED"));
        assertFalse(Files.exists(tempDir.resolve("tmp")));
        assertFalse(Files.exists(tempDir.resolve("data")));
    }

    @Test
    void flyIsOnlyRecognisedAsFirstArgument() throws IOException {
        Path input = tempDir.resolve("in.txt");
        Files.writeString(input, "HELP\n");
        String out = run("", input.toString(), "--fly");
        assertTrue(out.contains("Available commands"));
    }
}
