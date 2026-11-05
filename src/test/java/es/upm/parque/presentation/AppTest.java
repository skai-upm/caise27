package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.persistence.UserRepository;

/**
 * Tests for the application entry point and its command loop.
 */
public class AppTest {

    @Test
    public void entryPointStartsWithoutArgumentsAndEmptyInput() {
        InputStream original = System.in;
        try (InputStream empty = new ByteArrayInputStream(new byte[0])) {
            System.setIn(empty);
            assertDoesNotThrow(() -> App.main(new String[0]));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        } finally {
            System.setIn(original);
        }
    }

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    private static String[] startWith(String[] args, UserRepository repo, String stdin) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.start(args, TestSupport.processor(repo),
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        String text = out.toString(StandardCharsets.UTF_8).trim();
        return text.isEmpty() ? new String[0] : text.split("\\R");
    }

    @Test
    public void commandFileIsProcessedInOrderIgnoringBlankLines(@TempDir Path dir) throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, java.util.List.of(ANA, "", PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        UserRepository repo = new UserRepository();
        String[] lines = startWith(new String[] {input.toString()}, repo, "IGNORED STDIN");
        assertEquals(3, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("OK"), line);
        }
        assertEquals("699999999", repo.findByDni("12345678Z").getTelephone());
        assertEquals(2, repo.size());
    }

    @Test
    public void fileAndStandardInputProduceIdenticalOutput(@TempDir Path dir) throws Exception {
        String commands = String.join("\n", ANA, "FOO;bar", ANA, PEDRO, "USER DELETE;pedro@correo.com;Otra2#");
        Path input = dir.resolve("input.txt");
        Files.writeString(input, commands);
        UserRepository fromFile = new UserRepository();
        UserRepository fromStdin = new UserRepository();
        assertArrayEquals(startWith(new String[0], fromStdin, commands),
                startWith(new String[] {input.toString()}, fromFile, ""));
        assertEquals(fromStdin.size(), fromFile.size());
    }

    @Test
    public void noCommandRunsAfterExitForEitherSource(@TempDir Path dir) throws Exception {
        String commands = String.join("\n", ANA, "EXIT", PEDRO);
        Path input = dir.resolve("input.txt");
        Files.writeString(input, commands);
        UserRepository fromFile = new UserRepository();
        UserRepository fromStdin = new UserRepository();
        String[] fileLines = startWith(new String[] {input.toString()}, fromFile, "");
        String[] stdinLines = startWith(new String[0], fromStdin, commands);
        assertArrayEquals(stdinLines, fileLines);
        assertEquals(2, fileLines.length);
        assertTrue(fileLines[1].startsWith("OK"), fileLines[1]);
        assertEquals(1, fromFile.size());
        assertEquals(1, fromStdin.size());
        assertTrue(fromFile.existsByDni("12345678Z"));
    }

    private static String execute(String[] args, Path workDir, String stdin) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.execute(args, workDir, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    private static long tmpEntries(Path workDir) throws Exception {
        Path tmp = workDir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (java.util.stream.Stream<Path> s = Files.list(tmp)) {
            return s.count();
        }
    }

    @Test
    public void flyModeWithFileLeavesNormalDataUntouchedAndCleansUp(@TempDir Path work) throws Exception {
        execute(new String[0], work, ANA);
        String before = Files.readString(work.resolve("users.txt"));
        Path input = work.resolve("input.txt");
        Files.write(input, java.util.List.of(PEDRO, "GROUP CREATE;G01;Familia", "ATTRACTION CREATE;A01;Dragon;20"));
        String out = execute(new String[] {"--fly", input.toString()}, work, "");
        assertTrue(out.contains("OK: user 87654321X created"), out);
        assertEquals(before, Files.readString(work.resolve("users.txt")));
        assertTrue(!Files.exists(work.resolve("groups.txt")) || Files.readString(work.resolve("groups.txt")).isEmpty());
        assertTrue(!Files.exists(work.resolve("attractions.txt"))
                || Files.readString(work.resolve("attractions.txt")).isEmpty());
        assertEquals(0, tmpEntries(work));
        String again = execute(new String[] {"--fly", input.toString()}, work, "");
        assertTrue(again.contains("OK: user 87654321X created"), again);
    }

    @Test
    public void flyModeInteractiveWithExit(@TempDir Path work) throws Exception {
        String out = execute(new String[] {"--fly"}, work, String.join("\n", ANA, "EXIT", PEDRO));
        assertTrue(out.contains("OK: user 12345678Z created") && out.contains("OK: exiting application"), out);
        assertTrue(!Files.exists(work.resolve("users.txt")));
        assertEquals(0, tmpEntries(work));
    }

    @Test
    public void normalModeStillPersistsInWorkingDirectory(@TempDir Path work) throws Exception {
        execute(new String[0], work, String.join("\n", ANA, "GROUP CREATE;G01;Familia"));
        assertTrue(Files.readString(work.resolve("users.txt")).contains("12345678Z"));
        assertTrue(Files.readString(work.resolve("groups.txt")).startsWith("G01;Familia;"));
        String second = execute(new String[0], work, "GROUP CREATE;G01;Otro");
        assertTrue(second.contains("ERROR"), second);
    }

    @Test
    public void missingOrUnreadableFileIsReportedWithoutChanges(@TempDir Path dir) throws Exception {
        UserRepository repo = new UserRepository();
        String missing = dir.resolve("missing.txt").toString();
        String[] lines = startWith(new String[] {missing}, repo, ANA);
        assertEquals(1, lines.length);
        assertTrue(lines[0].startsWith("ERROR") && lines[0].contains("missing.txt"), lines[0]);

        lines = startWith(new String[] {dir.toString()}, repo, ANA);
        assertEquals(1, lines.length);
        assertTrue(lines[0].startsWith("ERROR") && lines[0].contains(dir.toString()), lines[0]);
        assertEquals(0, repo.size());
    }

    @Test
    public void commandsAreProcessedInOrderAndBlankLinesIgnored() throws Exception {
        UserRepository repo = new UserRepository();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String input = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "FOO;bar",
                "USER CREATE;Luis;Perez;87654321X;luis@correo.com;Clave1!;600000000;1234567890123457",
                "");
        App.run(TestSupport.processor(repo), new StringReader(input), new PrintStream(out, true, StandardCharsets.UTF_8));
        String[] lines = out.toString(StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("OK"));
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[2].startsWith("OK"));
        assertEquals(2, repo.size());
    }
}
