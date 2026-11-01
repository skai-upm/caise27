package es.upm.amusementpark.presentation;

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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppTest {

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER DELETE;pedro@correo.com;Wrong1!",
            "BOGUS",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private String runApp(String[] args, InputStream in, Path workDir) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, in, new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static InputStream input(List<String> lines) {
        return new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }

    private Path commandFile(List<String> lines) throws IOException {
        Path file = Files.createTempFile(dir, "input", ".txt");
        Files.write(file, lines);
        return file;
    }

    /** User file lines with the password column masked, since BCrypt salts differ per run. */
    private static List<String> withoutPasswords(Path usersFile) throws IOException {
        return Files.readAllLines(usersFile).stream()
                .map(line -> line.replaceFirst("^((?:[^;]*;){4})[^;]*", "$1*"))
                .toList();
    }

    private static boolean isEmptyDir(Path d) throws IOException {
        try (Stream<Path> s = Files.list(d)) {
            return s.findAny().isEmpty();
        }
    }

    @Test
    public void interactiveAndFileModesGiveSameResults() throws IOException {
        Path interactiveDir = Files.createDirectory(dir.resolve("interactive"));
        Path fileDir = Files.createDirectory(dir.resolve("file"));

        String interactiveOut = runApp(new String[0], input(COMMANDS), interactiveDir);
        String fileOut = runApp(new String[] {commandFile(COMMANDS).toString()}, noInput(), fileDir);

        assertEquals(interactiveOut, fileOut);
        assertEquals(withoutPasswords(interactiveDir.resolve("users.txt")),
                withoutPasswords(fileDir.resolve("users.txt")));
        assertTrue(Files.readString(fileDir.resolve("users.txt")).contains("699999999"));
    }

    @Test
    public void missingCommandFileIsReportedWithoutException() {
        String out = runApp(new String[] {dir.resolve("nope.txt").toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error: command file not found or not readable"));
    }

    @Test
    public void directoryAsCommandFileIsReported() {
        String out = runApp(new String[] {dir.toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error:"));
    }

    @Test
    public void usersAreRestoredOnNextExecution() throws IOException {
        runApp(new String[] {commandFile(List.of(COMMANDS.get(0), "EXIT")).toString()}, noInput(), dir);
        String out = runApp(new String[0], input(List.of(COMMANDS.get(0), "EXIT")), dir);
        assertTrue(out.contains("already exists"));
    }

    // --- --fly

    @Test
    public void flyRunDoesNotTouchPermanentDataAndCleansUp() throws IOException {
        runApp(new String[0], input(List.of(COMMANDS.get(0), "EXIT")), dir);
        String before = Files.readString(dir.resolve("users.txt"));

        String out = runApp(new String[] {"--fly"}, input(List.of(
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Changed",
                COMMANDS.get(1),
                "GROUP CREATE;G01;Familia",
                "ATTRACTION CREATE;A01;Tornado;20",
                "EXIT")), dir);

        assertTrue(out.contains("Error: Incorrect email or password"), "fly run starts with empty storage");
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
    }

    @Test
    public void flyWithCommandFileAndEndOfInputCleansUp() throws IOException {
        Path file = commandFile(List.of(COMMANDS.get(0), "GROUP CREATE;G01;Familia"));
        String out = runApp(new String[] {"--fly", file.toString()}, noInput(), dir);
        assertTrue(out.contains("User created"));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    public void flyWithMissingFileCleansUp() throws IOException {
        String out = runApp(new String[] {"--fly", dir.resolve("nope.txt").toString()}, noInput(), dir);
        assertTrue(out.startsWith("Error: command file not found"));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
    }

    @Test
    public void flyIsolatesDeliveryThreeData() throws IOException {
        runApp(new String[0], input(List.of(COMMANDS.get(0), "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "ATTRACTION CREATE;A01;Casa;5;SPECIAL", "EXIT")),
                dir);
        String users = Files.readString(dir.resolve("users.txt"));
        String tickets = Files.readString(dir.resolve("tickets.txt"));
        String attractions = Files.readString(dir.resolve("attractions.txt"));

        String out = runApp(new String[] {"--fly"}, input(List.of(COMMANDS.get(0), "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "SPEEDPASS CREATE;T001;10",
                "SPECIALPASS CREATE;T001", "ATTRACTION CREATE;A02;Rio;5;GROUP",
                "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#", "EXIT")), dir);

        assertTrue(out.contains("OK SPEEDPASS CREATED -> 80.00"));
        assertTrue(out.contains("OK SPECIALPASS CREATED -> 40.00"));
        assertEquals(users, Files.readString(dir.resolve("users.txt")));
        assertEquals(tickets, Files.readString(dir.resolve("tickets.txt")));
        assertEquals(attractions, Files.readString(dir.resolve("attractions.txt")));
        assertTrue(isEmptyDir(dir.resolve("tmp")));
    }

    @Test
    public void flyAndNormalRepliesAreIdentical() throws IOException {
        Path normalDir = Files.createDirectory(dir.resolve("normal"));
        Path flyDir = Files.createDirectory(dir.resolve("fly"));
        List<String> script = List.of(COMMANDS.get(0), "SYSTEM DATE;2026-10-01",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15", "USER DELETE;ana@correo.com;Clave1!", "EXIT");
        String normal = runApp(new String[0], input(script), normalDir);
        String fly = runApp(new String[] {"--fly"}, input(script), flyDir);
        assertEquals(normal, fly);
        assertTrue(normal.contains("OK TICKET CREATED -> 40.00"));
        assertTrue(Files.exists(normalDir.resolve("tickets.txt")));
        assertTrue(isEmptyDir(flyDir.resolve("tmp")));
    }

    // --- Delivery 3 integration

    private static final List<String> DELIVERY_3_SCENARIO = List.of(
            "SYSTEM DATE;2026-10-01",
            COMMANDS.get(0),
            COMMANDS.get(1),
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000000;1234567890123456",
            "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#",
            "GROUP CREATE;G01;Familia",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUP ADD;G01;ana@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15",
            "GROUPTICKET CREATE;TG01;G01;2026-10-15",
            "SPEEDPASS CREATE;T001;10",
            "SPECIALPASS CREATE;T001",
            "SPEEDPASS CREATE;TG01-1;1",
            "ATTRACTION CREATE;A01;Tornado;20",
            "ATTRACTION CREATE;A02;Casa;5;SPECIAL",
            "ATTRACTION CREATE;A03;Rio;10;GROUP",
            "SYSTEM DATE;2026-10-15",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A03;T001",
            "ATTRACTION ENTER;A02;T001;SPEED",
            "ATTRACTION ENTER;A02;T001",
            "ATTRACTION STATUS;A01",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "USER DELETE;ana@correo.com;Nueva2#",
            "HELP",
            "HELP EXTENDED",
            "EXIT",
            "USER DELETE;pedro@correo.com;Otra2#");

    @Test
    public void deliveryThreeScenarioIsConsistentAcrossModes() throws IOException {
        Path interactiveDir = Files.createDirectory(dir.resolve("e2e-interactive"));
        Path fileDir = Files.createDirectory(dir.resolve("e2e-file"));
        Path flyDir = Files.createDirectory(dir.resolve("e2e-fly"));
        String interactive = runApp(new String[0], input(DELIVERY_3_SCENARIO), interactiveDir);
        String file = runApp(new String[] {commandFile(DELIVERY_3_SCENARIO).toString()}, noInput(), fileDir);
        String fly = runApp(new String[] {"--fly"}, input(DELIVERY_3_SCENARIO), flyDir);

        assertEquals(interactive, file);
        assertEquals(interactive, fly);
        assertTrue(interactive.contains("OK GROUP TICKET CREATED -> 108.00"));
        assertTrue(interactive.contains("Ticket T001 entered attraction A01 consuming SpeedPass T001-SP1"));
        assertTrue(interactive.contains("Error: Ticket TG01-2 has no unused SpeedPass"));
        assertTrue(interactive.contains("Error: Attraction A03 (GROUP) does not admit ticket T001"));
        assertTrue(interactive.contains("Error: SPEED is not accepted at SPECIAL attraction A02"));
        assertTrue(interactive.contains("Ticket T001 entered attraction A02 consuming Special Pass T001-SPECIAL"));
        assertTrue(interactive.contains("TOTAL: 160.00"));
        assertTrue(interactive.contains("Error: User ana@correo.com has tickets dated on or after 2026-10-15"));
        for (CommandInfo c : CommandRegistry.commands()) {
            assertTrue(interactive.contains(c.description()), c.syntax());
        }
        assertFalse(interactive.contains("User deleted: pedro@correo.com"));
        assertTrue(isEmptyDir(flyDir.resolve("tmp")));
        assertFalse(Files.exists(flyDir.resolve("users.txt")));
        assertFalse(Files.readString(fileDir.resolve("users.txt")).contains("Nueva2#"));

        String restarted = runApp(new String[0], input(List.of("SYSTEM DATE;2026-10-15",
                "ATTRACTION STATUS;A02", "SPECIALPASS CREATE;T001", "INVOICE CREATE;USER;ana@correo.com")),
                interactiveDir);
        assertTrue(restarted.contains("Type: SPECIAL"));
        assertTrue(restarted.contains("Users present: 0"));
        assertTrue(restarted.contains("Error: Ticket T001 already acquired its Special Pass"));
        assertTrue(restarted.contains("TOTAL: 160.00"));
    }
}
