package es.upm.etsisi.park;

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
            "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456");

    @TempDir
    Path dir;
    private Path data;

    @BeforeEach
    void setUp() {
        data = dir.resolve("users.txt");
        System.setProperty(App.USERS_FILE_PROPERTY, data.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(App.USERS_FILE_PROPERTY);
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
        assertEquals(EXPECTED_DATA, Files.readAllLines(data));
    }

    @Test
    void fileModeProcessesTheGivenFile() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, INPUT);
        String out = run(new String[] {input.toString()}, stdin(""), 0);
        assertEquals(stdinOutputForSameSequence(), out);
        assertEquals(EXPECTED_DATA, Files.readAllLines(data));
    }

    private String stdinOutputForSameSequence() throws IOException {
        Path otherData = dir.resolve("other.txt");
        System.setProperty(App.USERS_FILE_PROPERTY, otherData.toString());
        try {
            return run(new String[0], stdin(INPUT), 0);
        } finally {
            System.setProperty(App.USERS_FILE_PROPERTY, data.toString());
        }
    }

    @Test
    void usersPersistBetweenRuns() throws IOException {
        run(new String[0], stdin(INPUT), 0);
        String out = run(new String[0],
                stdin("USER CREATE;Otra;Persona;12345678Z;otra@correo.com;Clave1!;600000000;1111222233334444\nEXIT"), 0);
        assertTrue(out.startsWith("ERROR: a user with DNI 12345678Z already exists"));
        assertEquals(EXPECTED_DATA, Files.readAllLines(data));
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
        assertTrue(out.startsWith("ERROR: cannot load users"));
    }

    @Test
    void mainClassContainsNoUserFileIo() throws IOException {
        String source = Files.readString(Path.of("src/main/java/es/upm/etsisi/park/App.java"));
        assertFalse(source.contains("newBufferedWriter"));
        assertFalse(source.contains("Files.write"));
        assertFalse(source.contains("readAllLines"));
    }
}
