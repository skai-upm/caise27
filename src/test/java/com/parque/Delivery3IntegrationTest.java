package com.parque;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.presentation.CommandCatalog;
import com.parque.presentation.CommandInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end verification of Delivery 3 with E1/E2 compatibility, using samples/input_fase3.txt.
 */
class Delivery3IntegrationTest {
    static final Path SCRIPT = Path.of("samples/input_fase3.txt");
    static final List<String> FILES = List.of("users.txt", "groups.txt", "tickets.txt", "grouptickets.txt",
            "attractions.txt", "passes.txt");

    /** First line of every response of the script (multi-line blocks are checked separately). */
    static final List<String> EXPECTED_STATUS = List.of(
            "OK: Available commands:",
            "OK: Available commands (extended):",
            "OK: System date set to 2026-10-01",
            "OK: User created",
            "OK: User created",
            "OK: User created",
            "OK: User created",
            "OK: User created",
            "ERROR: Invalid DNI: 12345678A",
            "ERROR: A user with email ana@correo.com already exists",
            "OK: User updated",
            "ERROR: Incorrect password",
            "OK: User updated",
            "OK: Group created",
            "OK: User added to group",
            "OK: User added to group",
            "OK: User added to group",
            "ERROR: No user with email nadie@correo.com",
            "OK: Group created",
            "OK TICKET CREATED -> 40.00",
            "ERROR: Ticket date 2026-09-01 is before the system date 2026-10-01",
            "OK: Ticket price set to 50.00",
            "OK TICKET CREATED -> 50.00",
            "OK: Ticket price set to 40.00",
            "OK GROUP TICKET CREATED -> 108.00",
            "ERROR: Group G02 has no members",
            "ERROR: Group G01 has group tickets dated on or after 2026-10-01 and cannot be modified or deleted",
            "ERROR: User pedro@correo.com has tickets dated on or after 2026-10-01 and cannot be deleted",
            "OK: SPEEDPASS CREATED -> 80.00",
            "ERROR: Invalid SpeedPass quantity: '3' (allowed: 1 or 10)",
            "OK: SPECIALPASS CREATED -> 40.00",
            "ERROR: Ticket T001 already has a Special Pass",
            "OK: SPEEDPASS CREATED -> 7.20",
            "OK: SPEEDPASS CREATED -> 7.20",
            "OK: SPEEDPASS CREATED -> 7.20",
            "OK: SPEEDPASS CREATED -> 10.00",
            "ERROR: Ticket TG01-1 belongs to group ticket TG01 and can only be deleted with it",
            "OK: Ticket deleted",
            "OK: SpeedPass refunded",
            "OK: Attraction created",
            "OK: Attraction created",
            "OK: Attraction created",
            "OK: Attraction created",
            "OK: Attraction created",
            "ERROR: Unknown attraction type: 'VIP' (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL)",
            "ERROR: Ticket T001 is valid for 2026-10-15, not for the system date 2026-10-01",
            "OK: System date set to 2026-10-15",
            "OK: 1 user(s) entered attraction A01; SpeedPass consumed: P01-1",
            "ERROR: GROUP attraction A02 does not admit ticket T001",
            "ERROR: INDIVIDUAL attraction A05 does not admit ticket TG01",
            "ERROR: Not enough places in attraction A03: 3 needed, 2 available",
            "OK: 3 user(s) entered attraction A02; SpeedPasses consumed: P11-1, P12-1, P13-1",
            "ERROR: The SPEED option is not accepted at SPECIAL attraction A04",
            "OK: 1 user(s) left attraction A01",
            "OK: 1 user(s) entered attraction A04; Special Pass consumed: S01",
            "OK: Attraction A02 - Familiar",
            "OK: Attraction A04 - Magia",
            "ERROR: Pass S01 has been consumed and cannot be refunded",
            "OK: Invoice for user Ana Garcia Lopez <ana@correo.com>",
            "OK: Invoice for group G01 - Familia Garcia",
            "ERROR: No user with email nadie@correo.com",
            "ERROR: Group G01 has group tickets dated on or after 2026-10-15 and cannot be modified or deleted",
            "OK: System date set to 2026-10-16",
            "OK: 3 user(s) left attraction A02",
            "OK: User removed from group",
            "OK: Group deleted",
            "OK: Exiting");

    @TempDir
    Path base;

    record Run(int status, List<String> out) {
    }

    private Run run(Path dir, String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        int status = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), ps, ps, dir);
        return new Run(status, out.toString(StandardCharsets.UTF_8).lines().toList());
    }

    private static List<String> statusLines(List<String> out) {
        return out.stream().filter(l -> !l.startsWith("  ")).toList();
    }

    private Map<String, byte[]> snapshot(Path dir) throws Exception {
        Map<String, byte[]> m = new HashMap<>();
        for (String f : FILES) {
            if (Files.exists(dir.resolve(f))) {
                m.put(f, Files.readAllBytes(dir.resolve(f)));
            }
        }
        return m;
    }

    @Test
    void scriptInFileMode() throws Exception {
        Run r = run(base, new String[] {SCRIPT.toAbsolutePath().toString()}, "");
        assertEquals(0, r.status());
        assertEquals(EXPECTED_STATUS, statusLines(r.out()));
        List<String> out = r.out();
        int invoice = out.indexOf("OK: Invoice for user Ana Garcia Lopez <ana@correo.com>");
        assertTrue(out.subList(invoice, invoice + 15).contains("  Total: 152.00"), out.toString());
        assertTrue(out.contains("    TG01 (2026-10-15, 3 members): 108.00"));
        assertTrue(out.contains("  Type: SPECIAL"));
        assertTrue(out.contains("    Pedro Lopez Martin <pedro@correo.com>"));
        assertEquals(List.of("G01;Familia Garcia;87654321X,22222222J"), Files.readAllLines(base.resolve("groups.txt")));
        assertEquals(List.of("TG01;G01;2026-10-15;40.00;108.00;TG01-1,TG01-2,TG01-3"),
                Files.readAllLines(base.resolve("grouptickets.txt")));
        List<String> passes = Files.readAllLines(base.resolve("passes.txt"));
        assertEquals(13, passes.size());
        assertTrue(passes.contains("SPECIAL;S01;T001;40.00;true"));
        assertTrue(passes.contains("SPEED;P11-1;TG01-1;7.20;true"));
        assertFalse(Files.readString(base.resolve("users.txt")).contains("Clave1!"));
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void sameResponsesFromStandardInputAndWithFly() throws Exception {
        String script = Files.readString(SCRIPT);
        Run file = run(base.resolve("a"), new String[] {SCRIPT.toAbsolutePath().toString()}, "");
        Run stdin = run(base.resolve("b"), new String[0], script);
        Run fly = run(base.resolve("c"), new String[] {"--fly", SCRIPT.toAbsolutePath().toString()}, "");
        assertEquals(file.out(), stdin.out());
        assertEquals(file.out(), fly.out());
        assertFalse(Files.exists(base.resolve("c").resolve("users.txt")));
    }

    @Test
    void flyLeavesPermanentStateUntouched() throws Exception {
        run(base, new String[] {SCRIPT.toAbsolutePath().toString()}, "");
        Map<String, byte[]> before = snapshot(base);
        Run fly = run(base, new String[] {"--fly", SCRIPT.toAbsolutePath().toString()}, "");
        assertEquals(EXPECTED_STATUS, statusLines(fly.out()), "fly starts from empty data");
        Map<String, byte[]> after = snapshot(base);
        assertEquals(before.keySet(), after.keySet());
        for (String f : before.keySet()) {
            assertArrayEquals(before.get(f), after.get(f), f);
        }
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void stateRestoredAfterRestart() throws Exception {
        run(base, new String[] {SCRIPT.toAbsolutePath().toString()}, "");
        Run next = run(base, new String[0], String.join("\n",
                "SYSTEM DATE;2026-10-01",
                "USER UPDATE;eva@correo.com;Eva5&a;PHONE;600000000",
                "USER UPDATE;eva@correo.com;Nueva2#;PHONE;600000000",
                "SPECIALPASS DELETE;S01",
                "GROUP ADD;G01;eva@correo.com",
                "SPEEDPASS DELETE;P01-2",
                "ATTRACTION ENTER;A02;T001",
                "INVOICE CREATE;GROUP;G01",
                "EXIT") + "\n");
        List<String> s = statusLines(next.out());
        assertEquals("ERROR: Incorrect password", s.get(1), "old password no longer valid");
        assertEquals("OK: User updated", s.get(2), "changed password restored as hash");
        assertTrue(s.get(3).startsWith("ERROR: Pass S01 has been consumed"));
        assertTrue(s.get(4).startsWith("ERROR: Group G01 has group tickets"), "lock restored");
        assertEquals("OK: SpeedPass refunded", s.get(5));
        assertTrue(s.get(6).startsWith("ERROR: GROUP attraction A02 does not admit"), "type restored");
        assertTrue(next.out().contains("  Total: 108.00"));
    }

    @Test
    void helpCoversEveryCommand() throws Exception {
        Run r = run(base, new String[0], "HELP\nHELP EXTENDED\nEXIT\n");
        List<CommandInfo> catalog = CommandCatalog.standard().commands();
        for (CommandInfo c : catalog) {
            assertTrue(r.out().contains("  " + c.syntax()), c.syntax());
            int i = r.out().lastIndexOf("  " + c.syntax());
            assertEquals("      " + c.description(), r.out().get(i + 1));
        }
        String source = Files.readString(Path.of("src/main/java/com/parque/presentation/CommandProcessor.java"));
        Matcher m = Pattern.compile("case \"([A-Z ]+)\" ->").matcher(source);
        Set<String> handled = new TreeSet<>();
        while (m.find()) {
            if (m.group(1).contains(" ") || List.of("HELP", "EXIT").contains(m.group(1))) {
                handled.add(m.group(1));
            }
        }
        handled.remove("USER");
        handled.remove("GROUP");
        Set<String> registered = catalog.stream().map(c -> c.syntax().split(";")[0])
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(registered, handled, "every handled command is registered for HELP and vice versa");
    }
}
