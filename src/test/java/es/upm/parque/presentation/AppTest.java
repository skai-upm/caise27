package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> COMMANDS = List.of(
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "",
        "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
        "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
        "EXIT",
        "USER DELETE;pedro@correo.com;Otra2#");

    @TempDir
    Path dir;

    private String run(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
            new PrintStream(bytes, true, StandardCharsets.UTF_8), dataDir, dir.resolve("tmp"));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileModeExecutesCommandsInOrderUntilExit() throws IOException {
        Path input = Files.write(dir.resolve("input_basico.txt"), COMMANDS);
        String output = run(new String[] {input.toString()}, "", dir);

        assertEquals(4, output.lines().count(), output);
        assertFalse(output.contains("ERROR"), output);
        List<String> stored = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals(2, stored.size());
        assertTrue(stored.get(0).contains(";699999999;"));
    }

    @Test
    void interactiveAndFileModesGiveTheSameResult() throws IOException {
        Path input = Files.write(dir.resolve("input.txt"), COMMANDS);
        String fromFile = run(new String[] {input.toString()}, "", dir.resolve("a"));
        String fromStdin = run(new String[0], String.join("\n", COMMANDS) + "\n", dir.resolve("b"));

        assertEquals(fromFile, fromStdin);
        assertEquals(withoutHashes(dir.resolve("a/users.txt")), withoutHashes(dir.resolve("b/users.txt")));
    }

    /** Password hashes are salted, so two runs store different hashes for the same password. */
    private static List<String> withoutHashes(Path usersFile) throws IOException {
        return Files.readAllLines(usersFile).stream().map(line -> line.replaceAll(";\\$2a\\$[^;]*;", ";HASH;")).toList();
    }

    @Test
    void interactiveModeEndsWithInput() {
        String output = run(new String[0], "HELP\n", dir);
        assertTrue(output.startsWith("Available commands"), output);
    }

    @Test
    void nonexistentFileIsReportedWithoutException() {
        Path missing = dir.resolve("no_existe.txt");
        String output = run(new String[] {missing.toString()}, "", dir);
        assertTrue(output.startsWith("ERROR: Cannot open command file") && output.contains("no_existe.txt"), output);
    }

    @Test
    void unreadableFileIsReportedWithoutException() {
        String output = run(new String[] {dir.toString()}, "", dir);
        assertTrue(output.startsWith("ERROR: Cannot"), output);
    }

    @Test
    void groupsAreRestoredOnRestart() throws IOException {
        Path users = dir;
        run(new String[0], COMMANDS.get(0) + "\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\n", users);
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
        String output = run(new String[0], "GROUP ADD;G01;ana@correo.com\n", users);
        assertTrue(output.contains("already in group"), output);
    }

    @Test
    void ticketsSurviveRestartAndPriceResets() throws IOException {
        Path users = dir;
        run(new String[0], COMMANDS.get(0) + "\nSYSTEM DATE;2026-10-01\nSYSTEM PRICE;60.00\n"
            + "TICKET CREATE;T001;ana@correo.com;2026-10-15\n", users);
        assertEquals(List.of("T001;2026-10-15;60.00;12345678Z"), Files.readAllLines(dir.resolve("tickets.txt")));
        String output = run(new String[0], "SYSTEM DATE;2026-10-01\nTICKET CREATE;T002;ana@correo.com;2026-10-16\n"
            + "USER DELETE;ana@correo.com;Clave1!\n", users);
        assertTrue(output.contains("OK TICKET CREATED -> 40.00"), output);
        assertTrue(output.contains("cannot be deleted"), output);
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        Path users = dir;
        run(new String[0], COMMANDS.get(0) + "\n" + COMMANDS.get(2) + "\nSYSTEM DATE;2026-10-01\n"
            + "GROUP CREATE;G01;Familia\nGROUP ADD;G01;ana@correo.com\nGROUP ADD;G01;pedro@correo.com\n"
            + "GROUPTICKET CREATE;TG01;G01;2026-10-15\n", users);
        assertEquals(List.of("TG01;2026-10-15;G01;40.00;0;TG01-1:12345678Z,TG01-2:87654321X"),
            Files.readAllLines(dir.resolve("group_tickets.txt")));
        String output = run(new String[0], "SYSTEM DATE;2026-10-01\nGROUP DELETE;G01\n", users);
        assertTrue(output.contains("cannot be modified"), output);
    }

    private List<Path> tmpContents() throws IOException {
        Path tmp = dir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return List.of();
        }
        try (var entries = Files.list(tmp)) {
            return entries.toList();
        }
    }

    @Test
    void flyModeIsIsolatedAndCleanedUpOnExit() throws IOException {
        Path data = dir.resolve("data");
        run(new String[0], COMMANDS.get(0) + "\n", data);
        List<String> permanent = Files.readAllLines(data.resolve("users.txt"));

        String output = run(new String[] {"--fly"}, COMMANDS.get(0) + "\n" + COMMANDS.get(2) + "\nEXIT\n", data);

        assertFalse(output.contains("ERROR"), output);
        assertEquals(permanent, Files.readAllLines(data.resolve("users.txt")));
        assertTrue(tmpContents().isEmpty());
    }

    @Test
    void flyModeWithFileUsesTemporaryFilesAndCleansUp() throws IOException {
        Path input = Files.write(dir.resolve("input_fase2.txt"),
            List.of(COMMANDS.get(0), "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        String output = run(new String[] {"--fly", input.toString()}, "", dir.resolve("data"));
        assertEquals(2, output.lines().filter(l -> l.startsWith("OK")).count(), output);
        assertFalse(Files.exists(dir.resolve("data/users.txt")));
        assertTrue(tmpContents().isEmpty());
    }

    @Test
    void flyModeCleansUpAfterFileError() throws IOException {
        String output = run(new String[] {"no_existe.txt", "--fly"}, "", dir.resolve("data"));
        assertTrue(output.startsWith("ERROR: Cannot open command file no_existe.txt"), output);
        assertTrue(tmpContents().isEmpty());
    }

    @Test
    void normalModeKeepsPermanentData() throws IOException {
        run(new String[0], COMMANDS.get(0) + "\nEXIT\n", dir.resolve("data"));
        assertEquals(1, Files.readAllLines(dir.resolve("data/users.txt")).size());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void consumedPassesSurviveRestart() throws IOException {
        Path users = dir;
        run(new String[0], COMMANDS.get(0) + "\nSYSTEM DATE;2026-10-15\nTICKET CREATE;T001;ana@correo.com;2026-10-16\n"
            + "SPEEDPASS CREATE;T001;1\nATTRACTION CREATE;A01;Tornado;5\nSYSTEM DATE;2026-10-16\n"
            + "ATTRACTION ENTER;A01;T001;SPEED\n", users);
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).get(0).endsWith(";CONSUMED"));
        String output = run(new String[0], "SYSTEM DATE;2026-10-01\nSPEEDPASS DELETE;T001-S1\n", users);
        assertTrue(output.contains("CONSUMED, not AVAILABLE"), output);
    }

    @Test
    void flyModeKeepsPassesAndAttractionsOutOfPermanentData() throws IOException {
        Path data = dir.resolve("data");
        String output = run(new String[] {"--fly"}, COMMANDS.get(0) + "\nSYSTEM DATE;2026-10-15\n"
            + "TICKET CREATE;T001;ana@correo.com;2026-10-15\nSPEEDPASS CREATE;T001;10\n"
            + "ATTRACTION CREATE;A01;Tornado;5;INDIVIDUAL\nEXIT\n", data);
        assertFalse(output.contains("ERROR"), output);
        assertFalse(Files.exists(data.resolve("passes.txt")));
        assertFalse(Files.exists(data.resolve("attractions.txt")));
        assertTrue(tmpContents().isEmpty());
    }

    @Test
    void attractionTypesAndPassesSurviveRestart() throws IOException {
        run(new String[0], COMMANDS.get(0) + "\nSYSTEM DATE;2026-10-15\nTICKET CREATE;T001;ana@correo.com;2026-10-15\n"
            + "SPECIALPASS CREATE;T001\nATTRACTION CREATE;A04;VIP;5;SPECIAL\nATTRACTION ENTER;A04;T001\n", dir);
        String output = run(new String[0], "SYSTEM DATE;2026-10-15\nATTRACTION STATUS;A04\nATTRACTION ENTER;A04;T001\n"
            + "INVOICE CREATE;USER;ana@correo.com\n", dir);
        assertTrue(output.contains("Attraction A04 - VIP (SPECIAL)"), output);
        assertTrue(output.contains("requires an available Special Pass"), output);
        assertTrue(output.contains("SpecialPass T001-SPECIAL (ticket T001): 40.00"), output);
    }
}
