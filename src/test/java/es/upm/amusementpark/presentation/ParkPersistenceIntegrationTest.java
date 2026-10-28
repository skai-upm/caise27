package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.persistence.file.DataLocation;

/**
 * Restarts the application on the same data and runs the temporary --fly mode.
 */
class ParkPersistenceIntegrationTest {

    @TempDir
    Path dir;

    private String run(Path data, String commands, String... args) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        assertEquals(0, App.execute(args, new BufferedReader(new StringReader(commands)), out, out, data,
                dir.resolve("tmp")));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static String lines(String... commands) {
        return String.join("\n", commands);
    }

    static final String SETUP = lines(
            "SYSTEM DATE;2027-04-15",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;6;1111222233334444",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUPTICKET CREATE;TG01;G01;2027-04-20",
            "SYSTEM PRICE;45.50",
            "TICKET CREATE;T001;luis@correo.com;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION ENTER;A01;T001");

    @Test
    void parkStateIsReconstructedOnRestart() throws Exception {
        Path data = dir.resolve("data");
        run(data, SETUP);
        String out = run(data, lines(
                "SYSTEM DATE;2027-04-15",
                "GROUP ADD;G01;luis@correo.com",
                "TICKET CREATE;TG01:12345678Z;luis@correo.com;2027-04-15",
                "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01",
                "ATTRACTION ENTER;A01;T001",
                "USER DELETE;luis@correo.com;Luis3$",
                "TICKET CREATE;T002;ana@correo.com;2027-04-15"));
        List<String> l = List.of(out.split("\\R"));
        assertTrue(l.get(1).startsWith("ERROR: Group is locked"), l.get(1));
        assertTrue(l.get(2).startsWith("ERROR: A ticket with this identifier"), l.get(2));
        assertEquals("NO USERS IN ATTRACTION A01", l.get(3));
        assertTrue(out.contains("Name: Dragon Rojo"));
        assertTrue(out.contains("USER ENTERED ATTRACTION: luis@correo.com"));
        assertTrue(out.contains("ERROR: User has active tickets"));
        assertTrue(out.contains("TICKET CREATED. Price: 40.00 EUR"));
        assertTrue(Files.readString(data.resolve("tickets.txt")).contains("I;T001;2027-04-15;45.50;11111111H"));
    }

    @Test
    void historicalTicketOfDeletedUserSurvivesRestart() throws Exception {
        Path data = dir.resolve("data");
        run(data, SETUP + "\nSYSTEM DATE;2027-04-16\nUSER DELETE;luis@correo.com;Luis3$");
        assertTrue(Files.readString(data.resolve("tickets.txt")).contains("I;T001;2027-04-15;45.50;11111111H;Luis;Perez;luis@correo.com"));
        String out = run(data, lines("SYSTEM DATE;2027-04-16",
                "TICKET CREATE;T001;ana@correo.com;2027-04-20",
                "GROUPTICKET DELETE;TG01"));
        assertTrue(out.startsWith("SYSTEM DATE UPDATED"));
        assertTrue(out.contains("ERROR: A ticket with this identifier already exists: T001"));
        assertTrue(out.contains("GROUPTICKET DELETED"));
    }

    @Test
    void flyModeUsesATemporaryDirectoryAndDeletesIt() throws Exception {
        Path data = dir.resolve("data");
        run(data, "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        List<String> permanent = Files.readAllLines(data.resolve("users.txt"));

        String interactive = run(data, lines(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "GROUP CREATE;G01;Tmp",
                "EXIT"), "--fly");
        assertTrue(interactive.startsWith("USER CREATED"), interactive);

        Path input = dir.resolve("input.txt");
        Files.writeString(input, lines(
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "ATTRACTION CREATE;A01;Dragon Rojo;20"));
        String fromFile = run(data, "", "--fly", input.toString());
        assertEquals(List.of("USER CREATED", "ATTRACTION CREATED"), List.of(fromFile.split("\\R")));

        assertEquals(permanent, Files.readAllLines(data.resolve("users.txt")));
        assertFalse(Files.exists(data.resolve("groups.txt")) && Files.readString(data.resolve("groups.txt")).contains("G01"));
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void flyDirectoryIsNamedByTimestamp() throws Exception {
        Path created = DataLocation.temporary(dir.resolve("tmp")).directory();
        assertTrue(created.getFileName().toString().matches("\\d{8}_\\d{6}"));
        Path second = DataLocation.temporary(dir.resolve("tmp")).directory();
        assertTrue(second.getFileName().toString().startsWith(created.getFileName().toString()));
        assertFalse(created.equals(second));
    }

    @Test
    void passwordsAreStoredHashedAndVerifiedAfterRestart() throws Exception {
        Path data = dir.resolve("data");
        run(data, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        String stored = Files.readString(data.resolve("users.txt"));
        assertFalse(stored.contains("Clave1!") || stored.contains("Nueva2#"));
        String out = run(data, String.join("\n",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;6",
                "USER UPDATE;ana@correo.com;Nueva2#;PHONE;6",
                "USER CREATE;Bea;X;87654321X;bea@correo.com;clave1!;6;1234567890123456",
                "USER DELETE;ana@correo.com;Nueva2#"));
        assertEquals(List.of("ERROR: Incorrect password", "USER UPDATED", "ERROR: Invalid password", "USER DELETED"),
                List.of(out.split("\\R")));
    }

    @Test
    void passesAndTheirStateSurviveRestarts() throws Exception {
        Path data = dir.resolve("data");
        run(data, String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T002;ana@correo.com;2027-04-20",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "SPEEDPASS CREATE;SP01;T001;1",
                "SPEEDPASS CREATE;SP02;T001;1",
                "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SP09;T002;10",
                "ATTRACTION ENTER;A01;T001;SPEED"));
        assertTrue(Files.readString(data.resolve("passes.txt")).contains("S;SP01:1;T001;8.00;true"));
        String out = run(data, String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SPECIALPASS CREATE;PE02;T001",
                "ATTRACTION ENTER;A01;T001;SPEED",
                "TICKET DELETE;T002"));
        List<String> l = List.of(out.split("\\R"));
        assertTrue(l.get(1).startsWith("ERROR: Ticket already has a Special Pass"), l.get(1));
        assertTrue(out.contains("SPEEDPASS CONSUMED: SP02:1 (ticket T001)"), out);
        assertTrue(out.contains("TICKET DELETED"));
        String third = run(data, "SYSTEM DATE;2027-04-15\nATTRACTION ENTER;A01;T001;SPEED\nSPEEDPASS DELETE;SP09:1");
        assertTrue(third.contains("ERROR: Ticket T001 has no unconsumed SpeedPass"), third);
        assertTrue(third.contains("ERROR: SpeedPass not found"), third);
        String passes = Files.readString(data.resolve("passes.txt"));
        assertTrue(passes.contains("P;PE01;T001;40.00;false"));
        assertFalse(passes.contains("SP09"));
    }

    @Test
    void flyModeDoesNotTouchPermanentPasses() throws Exception {
        Path data = dir.resolve("data");
        run(data, String.join("\n", "SYSTEM DATE;2027-04-15",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "SPEEDPASS CREATE;SP01;T001;10"), "--fly");
        assertTrue(Files.notExists(data.resolve("passes.txt")));
    }
}
