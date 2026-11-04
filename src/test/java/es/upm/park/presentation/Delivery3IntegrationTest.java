package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.service.ParkSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end verification of Deliveries 1-3 through the final configuration (F3-C10). */
class Delivery3IntegrationTest {

    static final Path SCENARIO = Path.of("src", "test", "resources", "integration", "delivery3.txt");
    static final Path EXPECTED = Path.of("src", "test", "resources", "integration", "delivery3.expected.txt");
    static final LocalDateTime START = LocalDateTime.of(2026, 11, 4, 10, 0, 0);
    static final List<String> DATA_FILES = List.of("users.txt", "groups.txt", "tickets.txt", "group-tickets.txt",
            "attractions.txt", "passes.txt");

    @TempDir
    Path dir;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    private String launch(Path workDir, String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.launch(args, workDir, START, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(0, status, err.toString(StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void everyCommandProducesItsExpectedResult() throws IOException {
        List<String> commands = Files.readAllLines(SCENARIO);
        List<String> expected = Files.readAllLines(EXPECTED);
        assertEquals(commands.size(), expected.size());
        try (ParkSession session = ParkSession.permanent(dir)) {
            CommandProcessor processor = App.newCommandProcessor(session.controller());
            for (int i = 0; i < commands.size(); i++) {
                String reply = processor.process(commands.get(i));
                assertTrue(reply.startsWith(expected.get(i)),
                        "line " + (i + 1) + ": " + commands.get(i) + "\n  expected: " + expected.get(i) + "\n  got: " + reply);
                if (expected.get(i).equals("OK")) {
                    assertFalse(reply.startsWith("OK ") && reply.contains("ERROR"));
                }
            }
            assertTrue(processor.isExitRequested());
        }
    }

    @Test
    void fileInteractiveAndFlyRunsAreEquivalent() throws IOException {
        String script = Files.readString(SCENARIO);
        Path fileDir = Files.createDirectories(dir.resolve("file"));
        Path interactiveDir = Files.createDirectories(dir.resolve("interactive"));
        Path flyDir = Files.createDirectories(dir.resolve("fly"));
        String fromFile = launch(fileDir, new String[] {SCENARIO.toAbsolutePath().toString()}, "");
        String interactive = launch(interactiveDir, new String[0], script);
        String fly = launch(flyDir, new String[] {"--fly", SCENARIO.toAbsolutePath().toString()}, "");
        assertEquals(fromFile, interactive);
        assertEquals(fromFile, fly);
        for (String f : DATA_FILES) {
            assertTrue(Files.exists(fileDir.resolve(f)), f + " persisted in normal mode");
            assertFalse(Files.exists(flyDir.resolve(f)), f + " must not be written by --fly");
        }
        try (Stream<Path> s = Files.list(flyDir.resolve("tmp"))) {
            assertEquals(0, s.count(), "fly run directory deleted");
        }
    }

    @Test
    void finalStateIsRestoredAfterRestartAndFlyDoesNotTouchIt() throws IOException {
        launch(dir, new String[] {SCENARIO.toAbsolutePath().toString()}, "");
        assertTrue(Files.readString(dir.resolve("users.txt")).contains(";$2a$10$"), "BCrypt hashes stored");
        assertTrue(Files.readString(dir.resolve("attractions.txt")).contains("A02;Saltos;3;GROUP"));
        assertTrue(Files.readString(dir.resolve("passes.txt")).contains("SPECIAL;SX1;T001;50.00;true"));
        assertFalse(Files.readString(dir.resolve("passes.txt")).contains("PD"), "cascade-deleted with T002");

        String again = launch(dir, new String[0], String.join("\n",
                "SYSTEM DATE;2026-10-15",
                "ATTRACTION STATUS;A03",
                "ATTRACTION ENTER;A03;T001",
                "ATTRACTION ENTER;A01;T001;SPEED",
                "INVOICE CREATE;GROUP;G01",
                "USER DELETE;ana@correo.com;Clave1!",
                "TICKET CREATE;T9;luis@correo.com;2026-10-20",
                "HELP")) ;
        String[] lines = again.split("\\R");
        assertTrue(again.contains("type: SPECIAL") && again.contains("present: 0"), "typed attraction, empty at start");
        assertTrue(again.contains("no unused Special Pass"), "consumed Special Pass restored");
        assertTrue(again.contains("SpeedPass consumed: PK-2"), "next unused SpeedPass after restart");
        assertTrue(again.contains("TG01 2026-10-15 (3 members, 10% discount): 135.00"), again);
        assertTrue(again.contains("has tickets dated on or after"), "restored ticket still protects its owner");
        assertTrue(again.contains("no registered user with email luis@correo.com"), "deleted user not restored");
        assertTrue(lines.length > 10);

        List<byte[]> before = new ArrayList<>();
        for (String f : DATA_FILES) {
            before.add(Files.readAllBytes(dir.resolve(f)));
        }
        launch(dir, new String[] {"--fly"}, "USER CREATE;Eva;S;22222222J;eva@correo.com;Clave1!;6;1234567890123456\n"
                + "SYSTEM DATE;2026-10-01\nTICKET CREATE;T8;eva@correo.com;2026-10-20\nSPEEDPASS CREATE;Q;T8;10\n"
                + "ATTRACTION CREATE;A09;X;1;GROUP\n");
        for (int i = 0; i < DATA_FILES.size(); i++) {
            assertTrue(java.util.Arrays.equals(before.get(i), Files.readAllBytes(dir.resolve(DATA_FILES.get(i)))),
                    DATA_FILES.get(i) + " unchanged by --fly");
        }
    }

    @Test
    void bothHelpVariantsListEveryAcceptedOperation() {
        try (ParkSession session = ParkSession.permanent(dir)) {
            CommandProcessor processor = App.newCommandProcessor(session.controller());
            String help = processor.process("HELP");
            String extended = processor.process("HELP EXTENDED");
            for (CommandInfo c : CommandCatalog.commands()) {
                assertTrue(help.contains("  " + c.syntax()), "HELP lists " + c.syntax());
                assertTrue(extended.contains("  " + c.syntax()), "HELP EXTENDED lists " + c.syntax());
            }
            for (String operation : new String[] {"USER CREATE", "USER DELETE", "USER UPDATE", "GROUP CREATE", "GROUP ADD",
                "GROUP REMOVE", "GROUP DELETE", "TICKET CREATE", "TICKET DELETE", "GROUPTICKET CREATE",
                "GROUPTICKET DELETE", "ATTRACTION CREATE", "ATTRACTION ENTER", "ATTRACTION EXIT", "ATTRACTION STATUS",
                "INVOICE CREATE", "SPEEDPASS CREATE", "SPEEDPASS DELETE", "SPECIALPASS CREATE", "SPECIALPASS DELETE",
                "SYSTEM DATE", "SYSTEM PRICE", "HELP", "HELP EXTENDED", "EXIT"}) {
                assertFalse(processor.process(operation + ";x;y;z;w;v;u;t;s").contains("unknown command"), operation);
                assertTrue(help.contains("  " + operation), "HELP shows " + operation);
            }
            assertTrue(processor.process("BOGUS COMMAND").contains("unknown command"));
        }
    }
}
