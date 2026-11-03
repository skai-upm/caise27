package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.help.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.help.CommandInfo;
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
import org.mindrot.jbcrypt.BCrypt;

/** End-to-end verification of Delivery 3 with E1/E2 compatibility, using samples/input_fase3.txt. */
class Delivery3IntegrationTest {

    static final Path SAMPLE = Path.of("samples/input_fase3.txt").toAbsolutePath();

    /** Expected responses, excluding the HELP / HELP EXTENDED / STATUS / INVOICE detail lines. */
    static final List<String> EXPECTED = List.of(
            "OK: User ana@correo.com created",
            "OK: User pedro@correo.com created",
            "OK: User luis@correo.com created",
            "ERROR: Invalid DNI: 12345678A",
            "OK: User ana@correo.com updated (PHONE)",
            "ERROR: Incorrect password",
            "OK: Group G01 created",
            "OK: User ana@correo.com added to group G01",
            "OK: User pedro@correo.com added to group G01",
            "OK: User luis@correo.com added to group G01",
            "OK: System date set to 2026-10-01",
            "OK TICKET CREATED -> 40.00",
            "OK: Ticket price set to 60.00",
            "OK TICKET CREATED -> 60.00",
            "OK: Ticket price set to 40.00",
            "OK GROUP TICKET CREATED -> 108.00",
            "ERROR: Group G01 is locked by a group ticket dated on or after 2026-10-01",
            "ERROR: User ana@correo.com cannot be deleted: it has tickets dated on or after 2026-10-01",
            "OK SPEEDPASS CREATED -> 80.00",
            "OK SPECIALPASS CREATED -> 40.00",
            "OK SPEEDPASS CREATED -> 7.20",
            "OK SPEEDPASS CREATED -> 7.20",
            "OK SPEEDPASS CREATED -> 7.20",
            "ERROR: SpeedPasses can only be bought in quantities of 1 or 10",
            "OK SPEEDPASS CREATED -> 12.00",
            "OK: Ticket T002 deleted",
            "OK: Attraction A01 created",
            "OK: Attraction A02 created",
            "OK: Attraction A03 created",
            "OK: Attraction A04 created",
            "OK: System date set to 2026-10-15",
            "OK: 1 user(s) entered attraction A01 (19 places available); consumed SpeedPass S1-1",
            "OK: 3 user(s) entered attraction A02 (7 places available); consumed SpeedPasses S2-1, S3-1, S4-1",
            "ERROR: Attraction A03 (INDIVIDUAL) does not admit ticket TG01",
            "ERROR: SPEED is not accepted at SPECIAL attraction A04",
            "OK: User(s) left attraction A01 (20 places available)",
            "OK: 1 user(s) entered attraction A04 (4 places available); consumed Special Pass SP1",
            "Attraction A02 - Coches",
            "Capacity: 10",
            "Users present: 3",
            "Available places: 7",
            "Present: Ana Garcia Lopez <ana@correo.com>, Pedro Lopez Martin <pedro@correo.com>, Luis Perez <luis@correo.com>",
            "INVOICE USER ana@correo.com (Ana Garcia Lopez) - 2026-10-15",
            "Individual tickets:",
            "Personal passes:",
            "Total: 167.20",
            "INVOICE GROUP G01 (Familia Garcia) - 2026-10-15",
            "Group tickets:",
            "Total: 108.00",
            "Available commands:",
            "Available commands (extended):",
            "OK: Bye");

    @TempDir
    Path dir;

    private List<String> run(Path work, String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.execute(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), work);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private static List<String> summary(List<String> out) {
        return out.stream().filter(l -> !l.startsWith("  ")).toList();
    }

    @Test
    void fileModeRunsEveryDeliveryTogether() {
        List<String> out = run(dir, "", SAMPLE.toString());
        assertEquals(EXPECTED, summary(out));
        assertTrue(out.contains("  T001 2026-10-15 40.00"));
        assertTrue(out.contains("  SP1 SPECIAL (ticket T001) 40.00"));
        assertTrue(out.contains("  S2-1 SPEED (ticket TG01-1) 7.20"));
        assertTrue(out.contains("  TG01 2026-10-15 3 members, 10% discount 108.00"));
        assertFalse(out.stream().anyMatch(l -> l.contains("after@correo.com")));
    }

    @Test
    void standardInputGivesTheSameResponses() throws IOException {
        List<String> file = run(dir.resolve("a"), "", SAMPLE.toString());
        List<String> stdin = run(dir.resolve("b"), Files.readString(SAMPLE));
        assertEquals(file, stdin);
    }

    @Test
    void finalStateIsRestoredAfterRestart() throws IOException {
        run(dir, "", SAMPLE.toString());
        CommandProcessor restarted = App.createProcessor(dir);
        restarted.process("SYSTEM DATE;2026-10-15");
        assertEquals(3, UserController.getInstance().getUsers().size());
        String anaLine = Files.readAllLines(dir.resolve("users.txt")).get(0);
        assertTrue(BCrypt.checkpw("Clave1!", anaLine.split(";")[4]));
        assertEquals(3, GroupController.getInstance().find("G01").orElseThrow().getMembers().size());
        assertTrue(TicketController.getInstance().find("T002").isEmpty());
        assertEquals(3, TicketController.getInstance().findGroupTicket("TG01").orElseThrow().getTickets().size());
        assertEquals(AttractionType.SPECIAL, AttractionController.getInstance().get("A04").getType());
        assertTrue(AttractionController.getInstance().get("A02").getPresent().isEmpty());
        PassController passes = PassController.getInstance();
        assertTrue(passes.find("S1-1").orElseThrow().isConsumed());
        assertFalse(passes.find("S1-2").orElseThrow().isConsumed());
        assertTrue(passes.find("SP1").orElseThrow().isConsumed());
        assertTrue(passes.find("S6-1").isEmpty());
        assertEquals("ERROR: Ticket T001 has no unused Special Pass", restarted.process("ATTRACTION ENTER;A04;T001"));
    }

    @Test
    void flyRunLeavesThePermanentDataUntouched() throws IOException {
        Path normal = dir.resolve("normal");
        Path fly = dir.resolve("fly");
        Files.createDirectories(fly);
        Files.write(fly.resolve("users.txt"), List.of("Old;User;44444444A;old@correo.com;x;600;4444555566667777"));
        List<String> expected = run(normal, "", SAMPLE.toString());
        List<String> isolated = run(fly, "", "--fly", SAMPLE.toString());
        assertEquals(expected, isolated);
        try (var files = Files.list(fly)) {
            assertEquals(List.of("users.txt"), files.map(f -> f.getFileName().toString()).toList());
        }
        assertEquals(List.of("Old;User;44444444A;old@correo.com;x;600;4444555566667777"),
                Files.readAllLines(fly.resolve("users.txt")));
    }

    @Test
    void bothHelpModesCoverEveryFinalCommand() {
        CommandProcessor p = App.createProcessor(dir);
        List<String> basic = p.process("HELP").lines().skip(1).map(String::trim).toList();
        List<String> extended = p.process("HELP EXTENDED").lines().skip(1).toList();
        assertEquals(28, basic.size());
        assertEquals(CommandCatalog.COMMANDS.stream().map(CommandInfo::getSyntax).toList(), basic);
        assertEquals(56, extended.size());
        for (CommandInfo c : CommandCatalog.COMMANDS) {
            assertTrue(extended.contains("      " + c.getDescription()), c.getSyntax());
            String operation = c.getSyntax().split(";")[0];
            if (!operation.equals("EXIT")) {
                assertFalse(p.process(operation).startsWith("ERROR: Unknown command"), operation);
            }
        }
    }
}
