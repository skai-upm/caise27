package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final List<String> SESSION = List.of(
            CommandRunnerTest.CREATE_ANA,
            CommandRunnerTest.CREATE_PEDRO,
            "",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!");

    @TempDir
    Path dir;

    /** Captured outcome of one application run. */
    private record Run(int status, String out, String err) {
    }

    private Run launch(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                dataDir);
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private Run interactive(String... lines) {
        return launch(new String[0], String.join("\n", lines), dir);
    }

    @Test
    void interactiveSessionStopsAtExit() throws IOException {
        Run r = interactive(SESSION.toArray(String[]::new));
        assertEquals(0, r.status());
        assertTrue(r.out().contains("Available commands:"));
        assertTrue(r.out().trim().endsWith("OK: Bye."));
        assertEquals(2, Files.readAllLines(dir.resolve("users.txt")).size(), "delete after EXIT must not run");
    }

    @Test
    void interactiveEndOfInputWithoutExit() {
        Run r = interactive(CommandRunnerTest.CREATE_ANA);
        assertEquals(0, r.status());
        assertEquals("OK: User ana@correo.com created.", r.out().trim());
    }

    @Test
    void fileModeMatchesInteractiveMode() throws IOException {
        Path commands = dir.resolve("input_basico.txt");
        Files.write(commands, SESSION);
        Path fileData = Files.createDirectory(dir.resolve("file"));
        Path stdinData = Files.createDirectory(dir.resolve("stdin"));

        Run fromFile = launch(new String[] {commands.toString()}, "", fileData);
        Run fromStdin = launch(new String[0], String.join("\n", SESSION), stdinData);

        assertEquals(0, fromFile.status());
        assertEquals(fromStdin.out(), fromFile.out());
        assertEquals(withoutPasswords(stdinData.resolve("users.txt")), withoutPasswords(fileData.resolve("users.txt")));
        assertTrue(Files.readString(fileData.resolve("users.txt")).contains("699999999"));
    }

    @Test
    void fileModeIgnoresStandardInput() throws IOException {
        Path commands = dir.resolve("cmds.txt");
        Files.write(commands, List.of("HELP"));
        Run r = launch(new String[] {commands.toString()}, CommandRunnerTest.CREATE_ANA, dir);
        assertEquals(0, r.status());
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void missingFileIsReportedWithoutStackTrace() {
        String missing = dir.resolve("nope.txt").toString();
        Run r = launch(new String[] {missing}, "", dir);
        assertNotEquals(0, r.status());
        assertTrue(r.err().contains("ERROR: Cannot read command file '" + missing + "'"));
        assertFalse(r.err().contains("\tat "), "no stack trace");
        assertTrue(r.out().isEmpty());
    }

    @Test
    void directoryPathIsReportedWithoutStackTrace() {
        Run r = launch(new String[] {dir.toString()}, "", dir);
        assertNotEquals(0, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"));
        assertFalse(r.err().contains("\tat "));
    }

    @Test
    void invalidPathIsReported() {
        Run r = launch(new String[] {"bad\u0000name"}, "", dir);
        assertNotEquals(0, r.status());
        assertTrue(r.err().startsWith("ERROR: Cannot read command file"));
    }

    @Test
    void groupsSurviveRestart() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, CommandRunnerTest.CREATE_PEDRO,
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "EXIT");
        Run second = interactive("GROUP ADD;G01;ana@correo.com", "GROUP REMOVE;G01;pedro@correo.com", "EXIT");
        assertTrue(second.out().startsWith("ERROR: User ana@correo.com is already in group G01."));
        assertEquals(List.of("G01;Familia Garcia;12345678Z"), Files.readAllLines(dir.resolve("groups.txt")));
    }

    @Test
    void ticketsPersistPriceResetsAndHistorySurvivesUserDeletion() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, "SYSTEM DATE;2026-10-01", "SYSTEM PRICE;60.00",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "EXIT");
        Run second = interactive("SYSTEM DATE;2026-10-01", "TICKET CREATE;T002;ana@correo.com;2026-10-20",
                "USER DELETE;ana@correo.com;Clave1!", "SYSTEM DATE;2026-10-21",
                "USER DELETE;ana@correo.com;Clave1!", "EXIT");
        List<String> out = second.out().lines().toList();
        assertEquals("OK TICKET CREATED -> 40.00", out.get(1), "price is not persisted");
        assertTrue(out.get(2).startsWith("ERROR: "), "active ticket blocks deletion");
        assertTrue(out.get(4).startsWith("OK: "), "past tickets do not block deletion");

        Run third = interactive("TICKET CREATE;T001;x@correo.com;2030-01-01", "EXIT");
        assertTrue(third.out().startsWith("ERROR: "));
        assertEquals(List.of("T001;2026-10-15;60.00;12345678Z", "T002;2026-10-20;40.00;12345678Z"),
                Files.readAllLines(dir.resolve("tickets.txt")));
    }

    @Test
    void groupTicketLockSurvivesRestart() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, CommandRunnerTest.CREATE_PEDRO, "SYSTEM DATE;2026-10-01",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15", "EXIT");
        Run second = interactive("SYSTEM DATE;2026-10-01", "GROUP REMOVE;G01;ana@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "EXIT");
        List<String> out = second.out().lines().toList();
        assertTrue(out.get(1).startsWith("ERROR: Group G01"));
        assertTrue(out.get(2).startsWith("ERROR: User pedro@correo.com"));
        assertEquals(List.of("TG01;2026-10-15;80.00;G01;40.00;12345678Z;87654321X"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void attractionsPersistButStartEmpty() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, "SYSTEM DATE;2026-10-15",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20",
                "ATTRACTION ENTER;A01;T001", "EXIT");
        Run second = interactive("ATTRACTION STATUS;A01", "EXIT");
        assertTrue(second.out().contains("Present: 0"));
        assertTrue(second.out().contains("Available: 20"));
        assertEquals(List.of("A01;Tornado;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
    }

    // ---- --fly mode ----

    private Run fly(String[] args, String stdin, Path dataDir, Path tmpRoot) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                dataDir, tmpRoot);
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private static long entries(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return 0;
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.count();
        }
    }

    @Test
    void flyRunLeavesPersistentDataUntouchedAndCleansUp() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, "EXIT");
        byte[] before = Files.readAllBytes(dir.resolve("users.txt"));
        Path tmpRoot = dir.resolve("tmp");
        Run r = fly(new String[] {"--fly"}, String.join("\n",
                CommandRunnerTest.CREATE_ANA, CommandRunnerTest.CREATE_PEDRO,
                "GROUP CREATE;G01;Familia", "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20", "EXIT"), dir, tmpRoot);
        assertEquals(0, r.status());
        assertTrue(r.out().startsWith("OK: User ana@correo.com created."), "fly data starts empty");
        assertTrue(r.err().startsWith("INFO: --fly mode, temporary data in "));
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(List.of("users.txt"), files.map(p -> p.getFileName().toString())
                    .filter(n -> !n.equals("tmp")).sorted().toList());
        }
        assertTrue(java.util.Arrays.equals(before, Files.readAllBytes(dir.resolve("users.txt"))));
        assertEquals(0, entries(tmpRoot), "run directory deleted");
    }

    @Test
    void flyRunUsesPersistenceWithinTheRun() throws IOException {
        Path tmpRoot = dir.resolve("tmp");
        Path commands = dir.resolve("input_fase2.txt");
        Files.write(commands, List.of(CommandRunnerTest.CREATE_ANA, CommandRunnerTest.CREATE_ANA, "EXIT"));
        Run r = fly(new String[] {"--fly", commands.toString()}, "", dir, tmpRoot);
        assertEquals(0, r.status());
        assertTrue(r.out().contains("ERROR: A user with DNI 12345678Z already exists."));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertEquals(0, entries(tmpRoot));
    }

    @Test
    void flyFlagMayFollowTheCommandFile() throws IOException {
        Path commands = dir.resolve("cmds.txt");
        Files.write(commands, List.of(CommandRunnerTest.CREATE_ANA));
        Run r = fly(new String[] {commands.toString(), "--fly"}, "", dir, dir.resolve("tmp"));
        assertEquals(0, r.status());
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void flyRunWithMissingFileCleansUp() throws IOException {
        Path tmpRoot = dir.resolve("tmp");
        Run r = fly(new String[] {"--fly", dir.resolve("missing.txt").toString()}, "", dir, tmpRoot);
        assertNotEquals(0, r.status());
        assertTrue(r.err().contains("ERROR: Cannot read command file"));
        assertEquals(0, entries(tmpRoot));
    }

    @Test
    void normalRunCreatesNoTemporaryDirectory() {
        Path tmpRoot = dir.resolve("tmp");
        fly(new String[0], CommandRunnerTest.CREATE_ANA, dir, tmpRoot);
        assertFalse(Files.exists(tmpRoot));
        assertTrue(Files.exists(dir.resolve("users.txt")));
    }

    /** User records with the password column (a salted hash, different on every run) removed. */
    private static List<String> withoutPasswords(Path usersFile) throws IOException {
        return Files.readAllLines(usersFile).stream()
                .map(l -> l.replaceFirst("^((?:[^;]*;){4})[^;]*", "$1<hash>"))
                .toList();
    }

    @Test
    void storedPasswordsAreHashes() throws IOException {
        interactive(CommandRunnerTest.CREATE_ANA, "EXIT");
        String line = Files.readAllLines(dir.resolve("users.txt")).get(0);
        assertFalse(line.contains("Clave1!"));
        assertTrue(line.split(";")[4].startsWith("$2a$10$"));
        Run second = interactive("USER DELETE;ana@correo.com;Clave1!", "EXIT");
        assertTrue(second.out().startsWith("OK: User ana@correo.com deleted."));
    }

    // ---- Delivery 3 persistence ----

    private static final String F3_SESSION = String.join("\n",
            CommandRunnerTest.CREATE_ANA, "SYSTEM DATE;2026-10-15",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15",
            "SPEEDPASS CREATE;SP01;T001;10", "SPECIALPASS CREATE;SPC1;T001",
            "ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION CREATE;A04;Vip;5;SPECIAL",
            "ATTRACTION ENTER;A01;T001;SPEED", "ATTRACTION ENTER;A04;T001", "EXIT");

    @Test
    void passesAndAttractionTypesSurviveRestart() throws IOException {
        Run first = interactive(F3_SESSION);
        assertTrue(first.out().contains("SpeedPasses consumed: SP01-1."), first.out());
        Run second = interactive("SYSTEM DATE;2026-10-15", "ATTRACTION ENTER;A04;T001",
                "ATTRACTION ENTER;A01;T001;SPEED", "INVOICE CREATE;USER;ana@correo.com", "EXIT");
        List<String> out = second.out().lines().toList();
        assertTrue(out.get(1).startsWith("ERROR: Ticket T001 has no unused Special Pass"), "SPECIAL type and consumed state kept");
        assertTrue(out.get(2).endsWith("SpeedPasses consumed: SP01-2."), "SP01-1 still consumed after restart");
        assertTrue(second.out().contains("SP01-1 SPEED (ticket T001) 8.00"), "price and ticket link kept");
        List<String> passes = Files.readAllLines(dir.resolve("passes.txt"));
        assertTrue(passes.contains("SPEED;SP01-1;T001;8.00;true"));
        assertTrue(passes.contains("SPECIAL;SPC1;T001;40.00;true"));
        assertTrue(Files.readAllLines(dir.resolve("attractions.txt")).contains("A04;Vip;5;SPECIAL"));
    }

    @Test
    void flyRunIsolatesDelivery3Data() throws IOException {
        interactive(F3_SESSION);
        java.util.Map<String, byte[]> before = new java.util.TreeMap<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(Files::isRegularFile).toList()) {
                before.put(f.getFileName().toString(), Files.readAllBytes(f));
            }
        }
        Path tmpRoot = dir.resolve("tmp");
        Run r = fly(new String[] {"--fly"}, F3_SESSION.replace("EXIT", "SPEEDPASS CREATE;SP09;T001;1\nEXIT"), dir, tmpRoot);
        assertEquals(0, r.status());
        assertTrue(r.out().contains("Special Pass consumed: SPC1."), "fly run starts from empty data");
        for (var e : before.entrySet()) {
            assertTrue(java.util.Arrays.equals(e.getValue(), Files.readAllBytes(dir.resolve(e.getKey()))), e.getKey());
        }
        assertEquals(0, entries(tmpRoot));
    }
}
