package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String INPUT = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444");

    private static final List<String> EXPECTED_DATA = List.of(
            "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;*;699999999;1234567890123456");

    @TempDir
    Path dir;
    private Path data;

    @BeforeEach
    void setUp() {
        data = dir.resolve("users.txt");
        System.setProperty(App.DATA_DIR_PROPERTY, dir.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(App.DATA_DIR_PROPERTY);
    }

    private static InputStream stdin(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private String run(String[] args, InputStream in, int expectedStatus) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int status = App.run(args, in, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertEquals(expectedStatus, status);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void interactiveModeReadsStandardInput() throws IOException {
        String out = run(new String[0], stdin(INPUT), 0);
        assertTrue(out.startsWith("USER CREATED"));
        assertTrue(out.strip().endsWith("BYE"));
        assertEquals(EXPECTED_DATA, TestParks.users(data));
    }

    @Test
    void fileModeProcessesTheGivenFile() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, INPUT);
        String out = run(new String[] {input.toString()}, stdin(""), 0);
        assertEquals(stdinOutputForSameSequence(), out);
        assertEquals(EXPECTED_DATA, TestParks.users(data));
    }

    private String stdinOutputForSameSequence() throws IOException {
        Path otherData = dir.resolve("other");
        System.setProperty(App.DATA_DIR_PROPERTY, otherData.toString());
        try {
            return run(new String[0], stdin(INPUT), 0);
        } finally {
            System.setProperty(App.DATA_DIR_PROPERTY, dir.toString());
        }
    }

    @Test
    void usersPersistBetweenRuns() throws IOException {
        run(new String[0], stdin(INPUT), 0);
        String out = run(new String[0],
                stdin("USER CREATE;Otra;Persona;12345678Z;otra@correo.com;Clave1!;600000000;1111222233334444\nEXIT"), 0);
        assertTrue(out.startsWith("ERROR: a user with DNI 12345678Z already exists"));
        assertEquals(EXPECTED_DATA, TestParks.users(data));
    }

    @Test
    void endOfInputWithoutExitTerminatesNormally() {
        String out = run(new String[0], stdin("HELP"), 0);
        assertFalse(out.contains("BYE"));
    }

    @Test
    void missingCommandFileIsReportedInAControlledWay() {
        String out = run(new String[] {dir.resolve("missing.txt").toString()}, stdin(""), 1);
        assertTrue(out.startsWith("ERROR: cannot read file"));
    }

    @Test
    void unreadableUsersFileIsReportedInAControlledWay() throws IOException {
        Files.createDirectories(data);
        String out = run(new String[0], stdin("EXIT"), 1);
        assertTrue(out.startsWith("ERROR: cannot load park data"));
    }

    @Test
    void mainClassContainsNoUserFileIo() throws IOException {
        String source = Files.readString(Path.of("src/main/java/es/upm/etsisi/park/App.java"));
        assertFalse(source.contains("newBufferedWriter"));
        assertFalse(source.contains("Files.write"));
        assertFalse(source.contains("readAllLines"));
    }

    private static final String PHASE2 = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "SYSTEM DATE;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION STATUS;A01");

    @Test
    void flyModeWithFileLeavesNoData() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, PHASE2);
        String out = run(new String[] {App.FLY, input.toString()}, stdin(""), 0);
        assertTrue(out.contains("GROUPTICKET CREATED. TOTAL PRICE: 40.00 EUR"));
        assertTrue(out.contains("INSIDE: 1"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(data));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
    }

    @Test
    void flyModeInteractiveUsesEmptyTemporaryData() throws IOException {
        run(new String[0], stdin(INPUT), 0);
        List<String> before = TestParks.users(data);
        String out = run(new String[] {App.FLY}, stdin("USER DELETE;ana.nueva@correo.com;Clave1!\n" + PHASE2 + "\nEXIT"), 0);
        assertTrue(out.startsWith("ERROR: no user with email ana.nueva@correo.com"));
        assertTrue(out.strip().endsWith("BYE"));
        assertEquals(before, TestParks.users(data));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyModeKeepsExistingTmpDirectory() throws IOException {
        Path tmp = Files.createDirectories(dir.resolve("tmp"));
        Files.writeString(tmp.resolve("keep.txt"), "x");
        run(new String[] {App.FLY}, stdin("EXIT"), 0);
        try (var entries = Files.list(tmp)) {
            assertEquals(List.of(tmp.resolve("keep.txt")), entries.toList());
        }
    }

    @Test
    void flyDirectoryIsTimestampedAndUnique() throws IOException {
        java.time.LocalDateTime now = java.time.LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        App.FlyDirectory first = App.FlyDirectory.create(dir.resolve("tmp"), now);
        App.FlyDirectory second = App.FlyDirectory.create(dir.resolve("tmp"), now);
        assertEquals(dir.resolve("tmp").resolve("20260826_190315"), first.getDirectory());
        assertEquals(dir.resolve("tmp").resolve("20260826_190315_1"), second.getDirectory());
        Files.writeString(first.getDirectory().resolve("users.txt"), "x");
        first.delete(System.out);
        assertTrue(Files.exists(dir.resolve("tmp")));
        second.delete(System.out);
        // only the run that created tmp/ removes it, and only when it is empty at that moment
        assertTrue(Files.exists(dir.resolve("tmp")));
        App.FlyDirectory third = App.FlyDirectory.create(dir.resolve("other"), now);
        third.delete(System.out);
        assertFalse(Files.exists(dir.resolve("other")));
    }

    @Test
    void normalFileModeKeepsPermanentData() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, PHASE2);
        run(new String[] {input.toString()}, stdin(""), 0);
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
    }

    @Test
    void flyModeWithMissingFileStillCleansUp() {
        String out = run(new String[] {App.FLY, dir.resolve("missing.txt").toString()}, stdin(""), 1);
        assertTrue(out.startsWith("ERROR: cannot read file"));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
