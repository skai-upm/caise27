package es.upm.etsisi.park;

import es.upm.etsisi.park.business.ParkTestSupport;
import es.upm.etsisi.park.presentation.CommandProcessorTest;

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
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final LocalDate TODAY = ParkTestSupport.TODAY;
    private static final String NL = System.lineSeparator();

    /** Reference sequence from the Delivery 1 assignment (source section 16). */
    static final String REFERENCE = String.join("\n",
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT") + "\n";

    /** Mixed Delivery 1 and 2 sequence in the style of input_fase2.txt, including rejected commands. */
    static final String PHASE2 = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;1",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;ana@correo.com;2027-04-14",
            "SYSTEM PRICE;45.50",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUP REMOVE;G01;pedro@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;ana@correo.com",
            "ATTRACTION ENTER;A01;T001",
            "ATTRACTION LIST;A01",
            "ATTRACTION STATUS;A01",
            "",
            "BOGUS;command",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!") + "\n";

    /** Delivery 3 sequence in the style of input_fase3.txt. */
    static final String PHASE3 = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "ATTRACTION CREATE;A05;Clasica;5",
            "SPEEDPASS CREATE;SP01;T001;1",
            "SPEEDPASS CREATE;SP02;T001;10",
            "SPEEDPASS CREATE;SP03;T001;3",
            "SPECIALPASS CREATE;PE01;T001",
            "SPECIALPASS CREATE;PE02;T001",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A03;TG01",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION STATUS;A05",
            "SPEEDPASS DELETE;SP02:5",
            "TICKET CREATE;T002;pedro@correo.com;2027-04-20",
            "SPEEDPASS CREATE;SP09;T002;1",
            "SPEEDPASS DELETE;SP09:1",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "HELP EXTENDED",
            "EXIT") + "\n";

    @TempDir
    Path dir;

    private record Result(int status, String out, String err) {
    }

    private Result run(String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8),
                dir, TODAY, ParkTestSupport.HASHER);
        return new Result(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private Path write(String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    private void deleteDataFiles() throws IOException {
        for (String f : List.of("users.txt", "groups.txt", "tickets.txt", "passes.txt", "attractions.txt")) {
            Files.deleteIfExists(dir.resolve(f));
        }
    }

    @Test
    void interactiveAndFileModesAreEquivalent() throws IOException {
        Path input = write("input.txt", PHASE2);
        Result fromFile = run(new String[] {input.toString()}, "");
        String usersAfterFile = Files.readString(dir.resolve("users.txt"));
        String ticketsAfterFile = Files.readString(dir.resolve("tickets.txt"));
        deleteDataFiles();
        Result fromStdin = run(new String[0], PHASE2);

        assertEquals(0, fromFile.status());
        assertEquals(0, fromStdin.status());
        assertEquals(fromStdin.out(), fromFile.out());
        assertEquals(usersAfterFile.lines().map(l -> l.replaceAll("\\$2a\\$[^;]*", "HASH")).toList(),
                Files.readString(dir.resolve("users.txt")).lines().map(l -> l.replaceAll("\\$2a\\$[^;]*", "HASH")).toList(),
                "same users except the random BCrypt salt");
        assertEquals(ticketsAfterFile, Files.readString(dir.resolve("tickets.txt")));
    }

    @Test
    void referenceExecutionProducesExpectedOutputAndState() throws IOException {
        Result r = run(new String[0], REFERENCE);
        List<String> lines = r.out().lines().toList();
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED", "Available commands:"),
                lines.subList(0, 6));
        assertEquals("BYE", lines.get(lines.size() - 1));
        List<String> stored = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals(1, stored.size());
        assertTrue(stored.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;$2a$"));
        assertTrue(stored.get(0).endsWith(";699999999;1234567890123456"));
    }

    @Test
    void phaseTwoSequenceKeepsInvariants() throws IOException {
        Result r = run(new String[0], PHASE2);
        List<String> lines = r.out().lines().toList();
        assertEquals("SYSTEM DATE SET TO 2027-04-15", lines.get(0));
        assertEquals("GROUP USER ADDED", lines.get(5));
        assertTrue(lines.get(6).startsWith("ERROR:"), "duplicate member");
        assertTrue(lines.get(10).startsWith("ERROR:"), "past ticket");
        assertEquals("GROUPTICKET CREATED TG01 MEMBERS 2 DISCOUNT 0% TOTAL 91.00 EUR", lines.get(12));
        assertTrue(lines.get(13).startsWith("ERROR:"), "locked group");
        assertTrue(lines.get(14).startsWith("ERROR:"), "user with active ticket");
        assertTrue(lines.get(15).startsWith("ERROR:"), "capacity 1 < group of 2");
        assertEquals("ATTRACTION ENTERED 2 USER(S)", lines.get(16));
        assertEquals("ATTRACTION EXITED", lines.get(17));
        assertEquals("ATTRACTION ENTERED 1 USER(S)", lines.get(18));
        assertTrue(r.out().contains("Inside: 2"));
        assertTrue(r.out().contains("Available: 18"));
        assertEquals("BYE", lines.get(lines.size() - 1));
        assertEquals(2, Files.readAllLines(dir.resolve("users.txt")).size(), "command after EXIT not run");
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL", "A02;Torre del Cielo;1;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
    }

    @Test
    void flyModeWithFileLeavesNoDataAndKeepsPermanentDataUntouched() throws IOException {
        run(new String[0], CommandProcessorTest.CREATE_ANA + "\nEXIT\n");
        List<String> before = permanentData();
        Path input = write("input_fase2.txt", PHASE2);

        Result r = run(new String[] {"--fly", input.toString()}, "");

        assertEquals(0, r.status());
        assertTrue(r.out().contains("GROUPTICKET CREATED TG01"), "fly run starts from empty state: " + r.out());
        assertEquals(before, permanentData(), "fly run wrote nothing to permanent storage");
        assertFalse(Files.exists(dir.resolve("tmp")), "temporary directory removed");
    }

    private List<String> permanentData() throws IOException {
        List<String> contents = new java.util.ArrayList<>();
        for (String f : List.of("users.txt", "groups.txt", "tickets.txt", "passes.txt", "attractions.txt")) {
            contents.add(f + "=" + Files.readString(dir.resolve(f)));
        }
        return contents;
    }

    @Test
    void flyModeInteractiveCleansUpAtExitAndEndOfInput() {
        Result withExit = run(new String[] {"--fly"}, CommandProcessorTest.CREATE_ANA + "\nEXIT\n");
        assertEquals("USER CREATED" + NL + "BYE" + NL, withExit.out());
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));

        Result endOfInput = run(new String[] {"--fly"}, CommandProcessorTest.CREATE_ANA + "\n");
        assertEquals("USER CREATED" + NL, endOfInput.out());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyModeCleansUpEvenWhenCommandFileIsMissing() {
        Result r = run(new String[] {"--fly", dir.resolve("nope.txt").toString()}, "");
        assertEquals(1, r.status());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void phaseThreeSequenceInAllModes() throws IOException {
        Path input = write("input_fase3.txt", PHASE3);
        Result r = run(new String[] {input.toString()}, "");
        assertEquals(0, r.status(), r.err());
        List<String> lines = r.out().lines().toList();
        assertEquals("SPEEDPASS CREATED SP01:1 TOTAL 8.00 EUR", lines.get(13));
        assertEquals("SPEEDPASS CREATED SP02:1..SP02:10 TOTAL 80.00 EUR", lines.get(14));
        assertTrue(lines.get(15).startsWith("ERROR:"), "quantity 3");
        assertEquals("SPECIALPASS CREATED PE01 PRICE 40.00 EUR", lines.get(16));
        assertTrue(lines.get(17).startsWith("ERROR:"), "second special pass");
        assertTrue(lines.get(18).startsWith("ERROR:"), "group ticket at INDIVIDUAL");
        assertEquals("ATTRACTION ENTERED 1 USER(S); SPEEDPASS CONSUMED: SP01:1", lines.get(19));
        assertTrue(lines.get(20).startsWith("ERROR:"), "members lack SpeedPasses");
        assertEquals("ATTRACTION ENTERED 2 USER(S)", lines.get(21));
        assertTrue(lines.get(22).startsWith("ERROR:"), "SPEED at SPECIAL");
        assertEquals("ATTRACTION ENTERED 1 USER(S); SPECIALPASS CONSUMED: PE01", lines.get(23));
        assertTrue(r.out().contains("Type: GENERAL"));
        assertTrue(r.out().contains("ERROR: SpeedPass can be refunded only for tickets dated after 2027-04-15"));
        assertTrue(lines.contains("SPEEDPASS DELETED"));
        assertTrue(r.out().contains("=== USER INVOICE ==="));
        assertTrue(r.out().contains("TOTAL: 168.00 EUR"), "T001 40 + SpeedPasses 8 + 80 + Special Pass 40: " + r.out());
        assertTrue(r.out().contains("=== GROUP INVOICE ==="));
        assertTrue(r.out().contains("TOTAL: 80.00 EUR"));
        assertTrue(r.out().contains("Available commands (extended):"));
        assertEquals("BYE", lines.get(lines.size() - 1));
        assertTrue(Files.readString(dir.resolve("passes.txt")).contains("S;SP01:1;T001;8.00;true"));

        List<String> before = permanentData();
        Result fly = run(new String[] {"--fly", input.toString()}, "");
        assertEquals(0, fly.status());
        assertEquals(r.out().lines().count(), fly.out().lines().count(), "fly starts from empty state too");
        assertEquals(before, permanentData());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void stateIsReloadedAcrossRuns() throws IOException {
        run(new String[0], CommandProcessorTest.CREATE_ANA + "\nEXIT\n");
        Result second = run(new String[0], CommandProcessorTest.CREATE_ANA + "\nUSER DELETE;ana@correo.com;Clave1!\n");
        assertTrue(second.out().startsWith("ERROR: DNI already registered"));
        assertTrue(second.out().contains("USER DELETED"));
        assertEquals("", Files.readString(dir.resolve("users.txt")));
    }

    @Test
    void missingOrDirectoryCommandFileIsControlled() {
        Result missing = run(new String[] {dir.resolve("nope.txt").toString()}, "");
        assertEquals(1, missing.status());
        assertTrue(missing.err().contains("Cannot read command file"));
        Result directory = run(new String[] {dir.toString()}, "");
        assertEquals(1, directory.status());
    }

    @Test
    void endOfInputWithoutExitEndsNormally() {
        Result r = run(new String[0], "HELP\n");
        assertEquals(0, r.status());
        assertTrue(r.out().contains("USER CREATE;"));
    }
}
