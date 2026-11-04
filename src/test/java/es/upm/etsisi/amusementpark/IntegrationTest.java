package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.CommandInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end Delivery 3 scenario in every execution mode (see src/test/resources/e2e). */
class IntegrationTest {

    private static final Path SCRIPT = Path.of("src", "test", "resources", "e2e", "delivery3.txt");
    private static final Path EXPECTED = Path.of("src", "test", "resources", "e2e", "delivery3.expected.txt");

    @TempDir
    Path dir;

    private static List<String> run(String[] args, String stdin, Path usersFile) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), usersFile);
        return bytes.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private static List<String> expected() throws IOException {
        return Files.readAllLines(EXPECTED, StandardCharsets.UTF_8);
    }

    @Test
    void fileModeProducesTheExpectedOutput() throws IOException {
        Path data = Files.createDirectory(dir.resolve("file"));
        assertEquals(expected(), run(new String[] {SCRIPT.toAbsolutePath().toString()}, "", data.resolve("users.txt")));
    }

    @Test
    void interactiveModeProducesTheSameOutput() throws IOException {
        Path data = Files.createDirectory(dir.resolve("interactive"));
        assertEquals(expected(), run(new String[0], Files.readString(SCRIPT), data.resolve("users.txt")));
    }

    @Test
    void flyModeMatchesAndLeavesPermanentDataUntouched() throws IOException {
        Path data = Files.createDirectory(dir.resolve("fly"));
        Path users = data.resolve("users.txt");
        run(new String[0], String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SPEEDPASS CREATE;SP1;T001;1", "ATTRACTION CREATE;A01;Tornado;20;INDIVIDUAL"), users);
        Map<Path, byte[]> permanent = new HashMap<>();
        try (Stream<Path> files = Files.list(data)) {
            for (Path f : files.filter(Files::isRegularFile).toList()) {
                permanent.put(f, Files.readAllBytes(f));
            }
        }
        assertEquals(expected(), run(new String[] {"--fly", SCRIPT.toAbsolutePath().toString()}, "", users));
        for (Map.Entry<Path, byte[]> e : permanent.entrySet()) {
            assertArrayEquals(e.getValue(), Files.readAllBytes(e.getKey()), e.getKey().toString());
        }
        try (Stream<Path> runs = Files.list(data.resolve("tmp"))) {
            assertTrue(runs.toList().isEmpty());
        }
    }

    @Test
    void bothHelpModesCoverEveryRegisteredCommand() throws IOException {
        List<String> out = run(new String[0], "HELP\nHELP EXTENDED\n", Files.createDirectory(dir.resolve("h")).resolve("users.txt"));
        String text = String.join("\n", out);
        int extendedStart = out.indexOf("OK: Available commands (extended):");
        String concise = String.join("\n", out.subList(0, extendedStart));
        String extended = String.join("\n", out.subList(extendedStart, out.size()));
        for (CommandInfo c : CommandCatalog.standard().commands()) {
            assertTrue(concise.contains("  " + c.syntax()), c.syntax());
            assertTrue(extended.contains("  " + c.syntax() + "\n      " + c.description()), c.syntax());
        }
        assertTrue(text.startsWith("OK: Available commands:"));
    }
}
