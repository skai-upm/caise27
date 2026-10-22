package es.upm.amusementpark;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "BAD COMMAND",
            "");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String stdin, Path workDir, String... args) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), workDir);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private Path subDir(String name) throws IOException {
        return Files.createDirectories(dir.resolve(name));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(COMMANDS, dir));
        assertTrue(output().startsWith("USER CREATED\nUSER CREATED\nUSER UPDATED\nUSER UPDATED\nUSER DELETED\nERROR"));
    }

    @Test
    void fileModeMatchesInteractiveMode() throws IOException {
        Path interactive = subDir("interactive");
        run(COMMANDS, interactive);
        String interactiveOutput = output();
        out.reset();

        Path input = dir.resolve("input.txt");
        Files.writeString(input, COMMANDS, StandardCharsets.UTF_8);
        Path fileMode = subDir("file");
        assertEquals(0, run("", fileMode, input.toString()));

        assertEquals(interactiveOutput, output());
        assertEquals(Files.readAllLines(interactive.resolve("users.txt")),
                Files.readAllLines(fileMode.resolve("users.txt")));
    }

    @Test
    void commandsAfterExitInFileAreNotExecuted() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                StandardCharsets.UTF_8);
        assertEquals(0, run("", dir, input.toString()));
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
        assertFalse(output().contains("USER CREATED\nUSER CREATED"));
    }

    @Test
    void missingCommandFileIsReportedWithoutStackTrace() {
        Path missing = dir.resolve("does-not-exist.txt");
        assertEquals(1, run("", dir, missing.toString()));
        assertTrue(output().startsWith("ERROR"));
        assertTrue(output().contains("does-not-exist.txt"));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("Exception"));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        assertEquals(1, run("", dir, dir.toString()));
        assertTrue(output().startsWith("ERROR"));
    }

    /** Standard input that records the temporary data files present when the input ends. */
    private static final class SpyInput extends ByteArrayInputStream {
        private final Path tmpRoot;
        private final List<String> seen = new ArrayList<>();

        SpyInput(String text, Path tmpRoot) {
            super(text.getBytes(StandardCharsets.UTF_8));
            this.tmpRoot = tmpRoot;
        }

        @Override
        public synchronized int read(byte[] buffer, int offset, int length) {
            int count = super.read(buffer, offset, length);
            if (count < 0 && seen.isEmpty()) {
                try (Stream<Path> files = Files.walk(tmpRoot)) {
                    files.filter(Files::isRegularFile).forEach(p -> seen.add(tmpRoot.relativize(p).toString()));
                } catch (IOException e) {
                    seen.add("unreadable");
                }
            }
            return count;
        }
    }

    @Test
    void flyInteractiveKeepsDataInTemporaryDirectoryAndCleansUp() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n");
        SpyInput input = new SpyInput(COMMANDS.replace("BAD COMMAND", "GROUP CREATE;G01;Familia"), dir.resolve("tmp"));
        int status = App.run(new String[] {"--fly"}, input, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), dir);

        assertEquals(0, status);
        assertTrue(output().startsWith("USER CREATED\nUSER CREATED\n"), "fly run starts without data");
        assertEquals(4, input.seen.size(), input.seen.toString());
        assertTrue(input.seen.stream().allMatch(name -> name.matches("\\d{8}_\\d{6}(_\\d+)?[/\\\\].+\\.txt")));
        assertEquals(List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyWithCommandFileCleansUpAfterExit() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        assertEquals(0, run("", dir, "--fly", input.toString()));
        assertEquals("USER CREATED\nATTRACTION CREATED\nBye\n", output());
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(List.of("input_fase2.txt"), files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void flyCleansUpWhenCommandFileIsMissing() {
        assertEquals(1, run("", dir, "--fly", dir.resolve("missing.txt").toString()));
        assertTrue(output().startsWith("ERROR"));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyOnlyCountsAsFirstArgument() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "ATTRACTION CREATE;A01;Dragon Rojo;20\n");
        assertEquals(0, run("", dir, input.toString(), "--fly"));
        assertTrue(Files.exists(dir.resolve("attractions.txt")));
    }

    @Test
    void unreadableUserFileIsReported() throws IOException {
        Files.createDirectory(dir.resolve("users.txt"));
        assertEquals(1, run("", dir));
        assertTrue(output().startsWith("ERROR"));
    }
}
