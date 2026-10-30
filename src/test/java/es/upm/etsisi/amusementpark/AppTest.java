package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.controller.ParkController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private static final List<String> EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    private static final List<String> PARK = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "SYSTEM DATE;2027-04-15",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION ENTER;A01;TG01");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    private final InputStream noInput = new ByteArrayInputStream(new byte[0]);

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Path commands(String name, List<String> lines) throws IOException {
        Path file = dir.resolve(name);
        Files.write(file, lines);
        return file;
    }

    @Test
    void fileModeRunsExampleAndPersists() throws IOException {
        Path commands = commands("input.txt", EXAMPLE);
        Path work = Files.createDirectory(dir.resolve("work"));

        assertEquals(0, App.run(new String[]{commands.toString()}, work, noInput, out));

        String output = output();
        assertEquals(2, output.split("USER CREATED", -1).length - 1);
        assertEquals(2, output.split("USER UPDATED", -1).length - 1);
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("USER DELETE;<Email>;<Password>"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(work.resolve("users.txt")));
    }

    @Test
    void interactiveModeReadsStandardInputWithSameProcessing() throws IOException {
        Path work = Files.createDirectory(dir.resolve("work"));
        InputStream in = new ByteArrayInputStream(String.join("\n", EXAMPLE).getBytes(StandardCharsets.UTF_8));

        assertEquals(0, App.run(new String[0], work, in, out));

        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(work.resolve("users.txt")));
    }

    @Test
    void dataSurvivesRestartAndUniquenessApplies() throws IOException {
        Path work = Files.createDirectory(dir.resolve("work"));
        App.run(new String[]{commands("first.txt", List.of(EXAMPLE.get(0))).toString()}, work, noInput, out);
        App.run(new String[]{commands("second.txt", List.of(
                "USER CREATE;Otra;Persona;11111111H;ana@correo.com;Clave1!;600000000;1111222233334444")).toString()},
                work, noInput, out);

        assertTrue(output().contains("ERROR: Email already registered"));
        assertEquals(1, Files.readAllLines(work.resolve("users.txt")).size());
    }

    @Test
    void missingCommandFileIsHandled() {
        int code = App.run(new String[]{dir.resolve("missing.txt").toString()}, dir, noInput, out);
        assertEquals(1, code);
        assertTrue(output().startsWith("ERROR: Command file not found"));
    }

    @Test
    void parkStateIsReconstructedWithEmptyAttractions() throws IOException {
        Path work = Files.createDirectory(dir.resolve("work"));
        App.run(new String[]{commands("park.txt", PARK).toString()}, work, noInput, out);
        assertTrue(output().contains("ATTRACTION ENTERED: 2 user(s)"));

        App.run(new String[]{commands("check.txt", List.of(
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION LIST;A01",
                "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-15",
                "USER DELETE;pedro@correo.com;Otra2#")).toString()}, work, noInput, out);

        String output = output();
        assertTrue(output.contains("NO USERS INSIDE"));
        assertTrue(output.contains("ERROR: Group G01 is locked by an active group ticket"));
        assertTrue(output.contains("ERROR: Ticket identifier already used: TG01:12345678Z"));
        assertTrue(output.contains("ERROR: User has active tickets"));
        ParkController park = ParkController.getInstance();
        assertEquals(2, park.tickets().findAll().size());
        assertEquals(2, park.groups().findById("G01").orElseThrow().getMembers().size());
    }

    @Test
    void flyModeLeavesNoDataAndDoesNotTouchNormalData() throws IOException {
        Path work = Files.createDirectory(dir.resolve("work"));
        Files.write(work.resolve("users.txt"), List.of(
                "Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"));
        Path commands = commands("park.txt", PARK);

        assertEquals(0, App.run(new String[]{App.FLY, commands.toString()}, work, noInput, out));

        assertTrue(output().contains("ATTRACTION ENTERED: 2 user(s)"));
        assertFalse(Files.exists(work.resolve(App.TMP_DIR)));
        assertEquals(List.of("users.txt"), listNames(work));
        assertEquals(1, Files.readAllLines(work.resolve("users.txt")).size());
    }

    @Test
    void flyInteractiveCleansUpOnExit() throws IOException {
        Path work = Files.createDirectory(dir.resolve("work"));
        InputStream in = new ByteArrayInputStream(String.join("\n", PARK.get(0), "EXIT")
                .getBytes(StandardCharsets.UTF_8));

        assertEquals(0, App.run(new String[]{App.FLY}, work, in, out));

        assertTrue(output().contains("USER CREATED"));
        assertEquals(List.of(), listNames(work));
    }

    @Test
    void flyDirectoryIsTimestampedAndUnique() throws IOException {
        Path root = dir.resolve(App.TMP_DIR);
        Path first = App.createFlyDirectory(root);
        Path second = App.createFlyDirectory(root);
        assertNotEquals(first, second);
        assertEquals(15, first.getFileName().toString().length());
        assertTrue(Files.isDirectory(second));
    }

    private static List<String> listNames(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }
}
