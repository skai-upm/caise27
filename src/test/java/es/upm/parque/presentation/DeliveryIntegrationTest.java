package es.upm.parque.presentation;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the Delivery 3 command script (E1, E2 and E3 commands) through the application entry point in every
 * execution mode and compares the output with the reviewed expected output.
 */
class DeliveryIntegrationTest {

    @TempDir
    Path dir;

    private String script;
    private List<String> expected;

    private static String resource(String name) throws IOException {
        try (InputStream in = DeliveryIntegrationTest.class.getResourceAsStream("/integration/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        script = resource("delivery3.txt");
        expected = resource("delivery3.expected").lines().toList();
    }

    private List<String> run(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
            new PrintStream(bytes, true, StandardCharsets.UTF_8), dataDir, dir.resolve("tmp"));
        return bytes.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void fileModePersistsEveryDeliveryFile() throws IOException {
        Path file = Files.writeString(dir.resolve("delivery3.txt"), script);
        Path data = dir.resolve("data");
        assertEquals(expected, run(new String[] {file.toString()}, "", data));
        for (String name : List.of("users.txt", "groups.txt", "tickets.txt", "group_tickets.txt", "passes.txt",
                "attractions.txt")) {
            assertTrue(Files.exists(data.resolve(name)), name);
        }
        assertTrue(Files.readString(data.resolve("passes.txt")).contains("SPECIAL;T001-SPECIAL;T001;40.00;CONSUMED"));
        assertTrue(Files.readString(data.resolve("attractions.txt")).contains("A04;VIP;5;SPECIAL"));
        assertFalse(Files.readString(data.resolve("users.txt")).contains("Clave1!"));
    }

    @Test
    void interactiveModeGivesTheSameOutput() {
        assertEquals(expected, run(new String[0], script, dir.resolve("data")));
    }

    @Test
    void flyModeGivesTheSameOutputAndLeavesNothing() throws IOException {
        Path file = Files.writeString(dir.resolve("delivery3.txt"), script);
        Path data = dir.resolve("data");
        assertEquals(expected, run(new String[] {"--fly", file.toString()}, "", data));
        assertFalse(Files.exists(data));
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void helpModesListEveryRegisteredCommand() {
        List<String> help = run(new String[0], "HELP\n", dir.resolve("data"));
        String extended = String.join("\n", run(new String[0], "HELP EXTENDED\n", dir.resolve("data")));
        for (CommandInfo command : CommandCatalog.all()) {
            assertTrue(help.contains("  " + command.syntax()), command.syntax());
            assertTrue(extended.contains(command.syntax()) && extended.contains(command.description()), command.syntax());
        }
    }
}
