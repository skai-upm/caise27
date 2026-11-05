package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs all E1, E2 and F3 commands together through the application entry point. */
class DeliveryIntegrationTest {

    @TempDir
    Path dir;

    private record Run(int status, String out, String err) {
    }

    private static Path commandFile() throws URISyntaxException {
        return Path.of(DeliveryIntegrationTest.class.getResource("/integration/input_fase3.txt").toURI());
    }

    private Run run(String[] args, String stdin, Path dataDir, Path tmpRoot) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8),
                dataDir, tmpRoot);
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private static Map<String, byte[]> snapshot(Path data) throws IOException {
        Map<String, byte[]> files = new TreeMap<>();
        try (Stream<Path> s = Files.list(data)) {
            for (Path f : s.filter(Files::isRegularFile).toList()) {
                files.put(f.getFileName().toString(), Files.readAllBytes(f));
            }
        }
        return files;
    }

    @Test
    void allDeliveriesWorkTogetherFromAFile() throws Exception {
        Path data = Files.createDirectory(dir.resolve("data"));
        Run r = run(new String[] {commandFile().toString()}, "", data, dir.resolve("tmp"));
        assertEquals(0, r.status());
        assertFalse(r.out().contains("ERROR"), r.out());
        String out = r.out();
        assertTrue(out.contains("OK: Ticket price set to 50.00."));
        assertTrue(out.contains("OK GROUP TICKET CREATED -> 135.00"));
        assertTrue(out.contains("OK: SpeedPasses SP01-1..SP01-10 purchased for ticket T001. Total: 100.00"));
        assertTrue(out.contains("Total: 200.00"), "user invoice with passes");
        assertTrue(out.contains("TG01 2026-10-15 (3 tickets, 10% discount) 135.00"));
        assertTrue(out.contains("SpeedPasses consumed: SP01-1."));
        assertTrue(out.contains("Special Pass consumed: SPC1."));
        assertTrue(out.contains("Present: 3"));
        assertTrue(out.contains("OK: Ticket T002 deleted. 1 pass(es) deleted."));
        assertTrue(out.contains("OK: Available commands (extended):"));
        assertTrue(out.trim().endsWith("OK: Bye."), "nothing after EXIT");

        List<String> users = Files.readAllLines(data.resolve("users.txt"));
        assertEquals(3, users.size(), "USER DELETE after EXIT not executed");
        assertFalse(String.join("\n", users).contains("Nueva2#"), "passwords hashed");
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X;11111111H"), Files.readAllLines(data.resolve("groups.txt")));
        assertEquals(List.of("T001;2026-10-15;50.00;12345678Z"), Files.readAllLines(data.resolve("tickets.txt")));
        assertTrue(Files.readAllLines(data.resolve("attractions.txt")).contains("A04;Vip;5;SPECIAL"));
        List<String> passes = Files.readAllLines(data.resolve("passes.txt"));
        assertEquals(12, passes.size());
        assertTrue(passes.contains("SPECIAL;SPC1;T001;50.00;true"));
    }

    @Test
    void interactiveRunMatchesFileRun() throws Exception {
        Path fileData = Files.createDirectory(dir.resolve("file"));
        Path stdinData = Files.createDirectory(dir.resolve("stdin"));
        Run fromFile = run(new String[] {commandFile().toString()}, "", fileData, dir.resolve("tmp"));
        Run fromStdin = run(new String[0], Files.readString(commandFile()), stdinData, dir.resolve("tmp"));
        assertEquals(fromFile.out(), fromStdin.out());
    }

    @Test
    void flyRunLeavesNormalDataUntouched() throws Exception {
        Path data = Files.createDirectory(dir.resolve("data"));
        run(new String[] {commandFile().toString()}, "", data, dir.resolve("tmp"));
        Map<String, byte[]> before = snapshot(data);
        Path tmpRoot = dir.resolve("tmp");
        Run r = run(new String[] {"--fly", commandFile().toString()}, "", data, tmpRoot);
        assertEquals(0, r.status());
        assertFalse(r.out().contains("ERROR"), "fly run starts empty, so the same file succeeds again");
        Map<String, byte[]> after = snapshot(data);
        assertEquals(before.keySet(), after.keySet());
        for (String name : before.keySet()) {
            assertTrue(Arrays.equals(before.get(name), after.get(name)), name);
        }
        try (Stream<Path> s = Files.list(tmpRoot)) {
            assertEquals(0, s.count(), "run directory removed");
        }
    }

    @Test
    void everyRegisteredCommandIsRecognisedAndDocumented() throws Exception {
        Path data = Files.createDirectory(dir.resolve("data"));
        StringBuilder input = new StringBuilder();
        for (CommandInfo c : CommandCatalog.standard().commands()) {
            String keyword = c.syntax().split(";")[0].trim();
            if (!keyword.equals("EXIT")) {
                input.append(keyword).append(";x\n");
            }
        }
        input.append("HELP\nHELP EXTENDED\nEXIT\n");
        Run r = run(new String[0], input.toString(), data, dir.resolve("tmp"));
        assertFalse(r.out().contains("Unknown command"), r.out());
        String help = r.out().substring(r.out().indexOf("OK: Available commands:"));
        String extended = r.out().substring(r.out().indexOf("OK: Available commands (extended):"));
        for (CommandInfo c : CommandCatalog.standard().commands()) {
            assertTrue(help.contains(c.syntax()), c.syntax());
            assertTrue(extended.contains(c.syntax() + System.lineSeparator() + "    " + c.description()), c.syntax());
        }
    }
}
