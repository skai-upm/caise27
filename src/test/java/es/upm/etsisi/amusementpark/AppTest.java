package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> SOURCE_EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true), new PrintStream(err, true), dir);
    }

    @Test
    void runsSourceExampleFromFile() throws Exception {
        Path input = Files.write(dir.resolve("input.txt"), SOURCE_EXAMPLE);
        assertEquals(0, run(new String[] {input.toString()}, ""));

        String output = out.toString();
        assertEquals(2, output.split("USER CREATED", -1).length - 1);
        assertEquals(2, output.split("USER UPDATED", -1).length - 1);
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("Available commands:"));
        assertFalse(output.contains("ERROR"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void runsInteractivelyFromStandardInputAndPersistsAcrossRuns() throws Exception {
        assertEquals(0, run(new String[0], String.join("\n", SOURCE_EXAMPLE.subList(0, 2)) + "\nEXIT\n"));
        assertEquals(0, run(new String[0], "USER DELETE;ana@correo.com;Clave1!\n"));
        assertTrue(out.toString().contains("USER DELETED"));
        assertEquals(List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void mixedValidAndInvalidInputKeepsStateConsistent() throws Exception {
        Path input = Files.write(dir.resolve("input.txt"), List.of(
                SOURCE_EXAMPLE.get(0),
                "NONSENSE",
                "USER CREATE;Ana;Garcia Lopez;12345678A;x@correo.com;Clave1!;1;1234567890123456",
                "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
                "USER UPDATE;ana@correo.com;Clave1!;CARD;1234",
                "USER DELETE;ana@correo.com;bad",
                "EXIT",
                SOURCE_EXAMPLE.get(1)));
        assertEquals(0, run(new String[] {input.toString()}, ""));
        assertEquals(5, out.toString().split("ERROR:", -1).length - 1);
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void missingCommandFileIsReportedWithoutException() {
        assertEquals(1, run(new String[] {dir.resolve("nope.txt").toString()}, ""));
        assertTrue(err.toString().contains("ERROR: cannot read command file"));
        assertEquals(1, run(new String[] {dir.toString()}, ""));
    }

    @Test
    void unreadableDataFileIsReported() throws Exception {
        Files.createDirectory(dir.resolve("users.txt"));
        assertEquals(1, run(new String[0], ""));
        assertTrue(err.toString().contains("ERROR: cannot read file"));
    }

    /** The mixed Delivery 1/2 file used for the integration and fly-mode tests. */
    private Path phaseTwoFile() throws Exception {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        List<String> lines = new ArrayList<>(SOURCE_EXAMPLE.subList(0, 2));
        lines.addAll(List.of(
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;" + today,
                "GROUPTICKET CREATE;TG01;G01;" + tomorrow,
                "GROUP REMOVE;G01;ana@correo.com",
                "USER DELETE;ana@correo.com;Clave1!",
                "ATTRACTION CREATE;A01;Dragon Rojo;1",
                "ATTRACTION ENTER;A01;T001",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION STATUS;A01",
                "SYSTEM DATE;" + tomorrow,
                "ATTRACTION EXIT;A01;ana@correo.com",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION CREATE;A02;Torre del Cielo;12",
                "ATTRACTION ENTER;A02;TG01",
                "ATTRACTION LIST;A02"));
        return Files.write(dir.resolve("input_fase2.txt"), lines);
    }

    @Test
    void mixedPhaseOneAndTwoFile() throws Exception {
        assertEquals(0, run(new String[] {phaseTwoFile().toString()}, ""));
        String output = out.toString();
        // GROUP REMOVE (locked), USER DELETE (active ticket), ENTER TG01 x2 (wrong date, then too few places)
        assertEquals(4, output.split("ERROR:", -1).length - 1, output);
        assertTrue(output.contains("GROUP TICKET CREATED. TOTAL PRICE: 80.00 EUR"));
        assertTrue(output.contains("ENTERED ATTRACTION: 2 USER(S)"));
        assertTrue(output.contains("ana@correo.com" + System.lineSeparator() + "pedro@correo.com"));

        // restart: everything is reloaded, attractions are empty
        out.reset();
        assertEquals(0, run(new String[0], "ATTRACTION STATUS;A02\nGROUP ADD;G01;ana@correo.com\n"));
        assertTrue(out.toString().contains("Occupied: 0"));
        assertTrue(out.toString().contains("ERROR: group G01 has an active group ticket"));
        assertEquals(3, Files.readAllLines(dir.resolve("tickets.txt")).size() - 1);
    }

    @Test
    void flyModeWithFileLeavesNoData() throws Exception {
        Path input = phaseTwoFile();
        assertEquals(0, run(new String[] {"--fly", input.toString()}, ""));
        assertTrue(out.toString().contains("GROUP TICKET CREATED"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void flyModeInteractiveDoesNotTouchPermanentData() throws Exception {
        Files.write(dir.resolve("input.txt"), SOURCE_EXAMPLE.subList(0, 1));
        assertEquals(0, run(new String[] {dir.resolve("input.txt").toString()}, ""));
        List<String> before = Files.readAllLines(dir.resolve("users.txt"));

        assertEquals(0, run(new String[] {"--fly"}, SOURCE_EXAMPLE.get(1) + "\nUSER DELETE;ana@correo.com;Clave1!\nEXIT\n"));
        assertTrue(out.toString().contains("ERROR: no user with email ana@correo.com"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertEquals(before, Files.readAllLines(dir.resolve("users.txt")));
    }

    @Test
    void flyModeWithMissingFileStillCleansUp() {
        assertEquals(1, run(new String[] {"--fly", dir.resolve("nope.txt").toString()}, ""));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
