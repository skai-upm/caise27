package es.upm.park;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    static final String INPUT_BASICO = String.join("\n",
            CommandProcessorTest.CREATE_ANA,
            CommandRunnerTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "EXIT") + "\n";

    @TempDir
    Path dir;

    /** Result of one application run. */
    record Run(int status, String out, String err) {
    }

    private Run run(String[] args, Path usersFile, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        App.DataFiles files = new App.DataFiles(usersFile, usersFile.resolveSibling("groups-" + usersFile.getFileName()),
                usersFile.resolveSibling("tickets-" + usersFile.getFileName()),
                usersFile.resolveSibling("group-tickets-" + usersFile.getFileName()),
                usersFile.resolveSibling("attractions-" + usersFile.getFileName()));
        int status = App.run(args, files, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void interactiveSessionStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Run r = run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n" + CommandRunnerTest.CREATE_PEDRO + "\n");
        assertEquals(0, r.status());
        assertTrue(r.out().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(r.out().strip().endsWith("OK: application terminated"));
        List<String> lines = Files.readAllLines(users);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(";699999999;"));
    }

    @Test
    void fileModeRunsInputBasico() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, INPUT_BASICO);
        Run r = run(new String[] {input.toString()}, users, "");
        assertEquals(0, r.status());
        assertEquals("", r.err());
        assertEquals(4, r.out().split("\\R").length);
        List<String> lines = Files.readAllLines(users);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains(";699999999;"));
    }

    @Test
    void fileModeSkipsBlankLinesAndStopsAtExit() throws IOException {
        Path users = dir.resolve("users.txt");
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, CommandProcessorTest.CREATE_ANA + "\n\nHELP\nEXIT\n" + CommandRunnerTest.CREATE_PEDRO + "\n");
        Run r = run(new String[] {input.toString()}, users, "");
        assertEquals(0, r.status());
        assertFalse(r.out().contains("ERROR"));
        assertEquals(1, Files.readAllLines(users).size());
    }

    @Test
    void sameCommandsGiveSameResultsInBothModes() throws IOException {
        String commands = CommandProcessorTest.CREATE_ANA + "\nbad command\n" + CommandRunnerTest.CREATE_PEDRO
                + "\nUSER DELETE;pedro@correo.com;Mala1!\nUSER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666\nHELP\n";
        Path usersA = dir.resolve("a.txt");
        Path usersB = dir.resolve("b.txt");
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, commands);
        Run interactive = run(new String[0], usersA, commands);
        Run file = run(new String[] {input.toString()}, usersB, "");
        assertEquals(interactive.out(), file.out());
        assertEquals(Files.readAllLines(usersA), Files.readAllLines(usersB));
    }

    @Test
    void missingFileIsReportedWithoutException() {
        Run r = run(new String[] {dir.resolve("nope.txt").toString()}, dir.resolve("users.txt"), "");
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR") && r.err().contains("nope.txt"), r.err());
        assertFalse(r.err().contains("Exception"));
        assertEquals("", r.out());
    }

    @Test
    void directoryIsReportedWithoutException() {
        Run r = run(new String[] {dir.toString()}, dir.resolve("users.txt"), "");
        assertEquals(1, r.status());
        assertTrue(r.err().startsWith("ERROR"));
        assertFalse(r.err().contains("Exception"));
    }

    @Test
    void unreadableFileIsReported() throws IOException {
        Path input = dir.resolve("locked.txt");
        Files.writeString(input, "HELP\n");
        boolean changed = input.toFile().setReadable(false);
        if (changed && !Files.isReadable(input)) {
            Run r = run(new String[] {input.toString()}, dir.resolve("users.txt"), "");
            assertEquals(1, r.status());
            assertTrue(r.err().startsWith("ERROR"));
        }
        input.toFile().setReadable(true);
    }

    @Test
    void groupsSurviveRestart() throws IOException {
        Path users = dir.resolve("users.txt");
        run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\nGROUP CREATE;G01;Familia Garcia\n"
                + "GROUP ADD;G01;ana@correo.com\nEXIT\n");
        Run second = run(new String[0], users, "GROUP ADD;G01;ana@correo.com\nGROUP REMOVE;G01;ana@correo.com\n");
        String[] lines = second.out().split("\\R");
        assertTrue(lines[0].startsWith("ERROR"), "membership restored, so duplicate add is rejected");
        assertTrue(lines[1].startsWith("OK"));
    }

    @Test
    void restoredTicketBlocksDeletionAndPriceResets() {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\nSYSTEM DATE;2026-10-01\n"
                + "SYSTEM PRICE;60.00\nTICKET CREATE;T001;ana@correo.com;2026-10-15\n");
        assertTrue(first.out().contains("OK TICKET CREATED -> 60.00"), first.out());
        Run second = run(new String[0], users, "SYSTEM DATE;2026-10-01\nUSER DELETE;ana@correo.com;Clave1!\n"
                + "TICKET CREATE;T002;ana@correo.com;2026-10-20\nSYSTEM DATE;2026-10-16\nUSER DELETE;ana@correo.com;Clave1!\n");
        String[] lines = second.out().split("\\R");
        assertTrue(lines[1].startsWith("ERROR"), lines[1]);
        assertEquals("OK TICKET CREATED -> 40.00", lines[2]);
        assertTrue(lines[4].startsWith("ERROR"), "T002 (2026-10-20) still blocks: " + lines[4]);
    }

    @Test
    void groupTicketLocksSurviveRestart() {
        Path users = dir.resolve("users.txt");
        Run first = run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\n" + CommandRunnerTest.CREATE_PEDRO
                + "\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\nGROUP ADD;G01;pedro@correo.com\n"
                + "SYSTEM DATE;2026-10-01\nGROUPTICKET CREATE;TG01;G01;2026-10-15\n");
        assertTrue(first.out().contains("OK GROUP TICKET CREATED -> 80.00"), first.out());
        Run second = run(new String[0], users, "SYSTEM DATE;2026-10-01\nGROUP DELETE;G01\n"
                + "USER DELETE;pedro@correo.com;Otra2#\nGROUPTICKET DELETE;TG01\nGROUP DELETE;G01\n");
        String[] lines = second.out().split("\\R");
        assertTrue(lines[1].startsWith("ERROR"), lines[1]);
        assertTrue(lines[2].startsWith("ERROR"), lines[2]);
        assertTrue(lines[3].startsWith("OK"), lines[3]);
        assertTrue(lines[4].startsWith("OK"), lines[4]);
    }

    @Test
    void attractionsRestartEmpty() {
        Path users = dir.resolve("users.txt");
        run(new String[0], users, CommandProcessorTest.CREATE_ANA + "\nSYSTEM DATE;2026-10-15\n"
                + "TICKET CREATE;T001;ana@correo.com;2026-10-15\nATTRACTION CREATE;A01;Tornado;20\nATTRACTION ENTER;A01;T001\n");
        Run second = run(new String[0], users, "ATTRACTION STATUS;A01\n");
        assertTrue(second.out().contains("Tornado") && second.out().contains("present: 0"), second.out());
    }

    static final LocalDateTime START = LocalDateTime.of(2026, 8, 26, 19, 3, 15);

    private Run launch(String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status = App.launch(args, dir, START, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private long tmpEntries() throws IOException {
        Path tmp = dir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> s = Files.list(tmp)) {
            return s.count();
        }
    }

    @Test
    void flyInteractiveUsesTemporaryDataAndLeavesPermanentFilesUntouched() throws IOException {
        launch(new String[0], CommandProcessorTest.CREATE_ANA + "\n");
        byte[] before = Files.readAllBytes(dir.resolve("users.txt"));

        Run fly = launch(new String[] {"--fly"}, "USER DELETE;ana@correo.com;Clave1!\n"
                + CommandRunnerTest.CREATE_PEDRO + "\nUSER UPDATE;pedro@correo.com;Otra2#;NAME;Pedro Jose\nEXIT\n");
        String[] lines = fly.out().split("\\R");
        assertTrue(lines[0].startsWith("ERROR"), "fly run must not see permanent Ana: " + lines[0]);
        assertTrue(lines[2].startsWith("OK"), "update works on temporary data: " + lines[2]);
        assertEquals(0, fly.status());
        assertEquals(0, tmpEntries(), "run directory deleted after EXIT");
        assertTrue(java.util.Arrays.equals(before, Files.readAllBytes(dir.resolve("users.txt"))));
        assertFalse(Files.exists(dir.resolve("groups.txt")) && Files.readString(dir.resolve("groups.txt")).contains("Pedro"));
    }

    @Test
    void flyFileModeOperatesOnTemporaryData() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, CommandProcessorTest.CREATE_ANA + "\nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
                + "ATTRACTION CREATE;A01;Tornado;20\n");
        Run fly = launch(new String[] {"--fly", input.toString()}, "");
        assertEquals(0, fly.status());
        assertEquals(3, fly.out().split("\\R").length);
        assertFalse(fly.out().contains("ERROR"), fly.out());
        assertFalse(Files.exists(dir.resolve("users.txt")), "no permanent file written");
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
        assertEquals(0, tmpEntries(), "run directory deleted at end of file");
    }

    @Test
    void flyWithMissingFileCleansUp() throws IOException {
        Run fly = launch(new String[] {"--fly", dir.resolve("missing.txt").toString()}, "");
        assertEquals(1, fly.status());
        assertTrue(fly.err().startsWith("ERROR"));
        assertEquals(0, tmpEntries());
    }

    @Test
    void flyRunDirectoryExistsDuringTheRun() throws IOException {
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, CommandProcessorTest.CREATE_ANA + "\n");
        Path expected = dir.resolve("tmp").resolve("20260826_190315").resolve("users.txt");
        java.io.InputStream probe = new java.io.InputStream() {
            @Override
            public int read() {
                return -1;
            }
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream recording = new PrintStream(out, true, StandardCharsets.UTF_8) {
            @Override
            public void println(String x) {
                super.println(x + (Files.exists(expected) ? " [in tmp]" : " [missing]"));
            }
        };
        App.launch(new String[] {"--fly", input.toString()}, dir, START, probe, recording, System.err);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("[in tmp]"), out.toString(StandardCharsets.UTF_8));
        assertFalse(Files.exists(expected));
    }

    @Test
    void normalModeCreatesNoTmpDirectory() throws IOException {
        Run r = launch(new String[0], CommandProcessorTest.CREATE_ANA + "\n");
        assertEquals(0, r.status());
        assertTrue(Files.exists(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
