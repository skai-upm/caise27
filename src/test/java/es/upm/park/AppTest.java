package es.upm.park;

import es.upm.park.business.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private static final String COMMANDS = "HELP\nEXIT\nHELP\n";

    @TempDir
    Path base;

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String[] runApp(String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        App.run(args, base, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
        return new String[] {out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8)};
    }

    private Path commandFile(String name, String content) throws IOException {
        Path file = base.resolve(name);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private long temporaryDirectories() throws IOException {
        Path tmp = base.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> entries = Files.list(tmp)) {
            return entries.count();
        }
    }

    @Test
    void fileAndStandardInputProduceIdenticalOutput() throws IOException {
        Path file = commandFile("input.txt", COMMANDS);
        String fromStdin = runApp(new String[0], COMMANDS)[0];
        String fromFile = runApp(new String[] {file.toString()}, "")[0];
        assertEquals(fromStdin, fromFile);
        assertTrue(fromFile.contains("Available commands:"));
    }

    @Test
    void unreadableFileIsReportedInControlledManner() {
        String[] result = runApp(new String[] {base.resolve("missing.txt").toString()}, "");
        assertEquals("", result[0]);
        assertTrue(result[1].contains("Cannot read command file"));
    }

    @Test
    void normalRunsPersistAcrossExecutions() {
        runApp(new String[0], "GROUP CREATE;G01;Familia Garcia\nATTRACTION CREATE;A01;Dragon Rojo;20\nEXIT\n");
        ParkControllerTestSupport.reset();
        String out = runApp(new String[0], "GROUP CREATE;G01;Otro\nATTRACTION STATUS;A01\nEXIT\n")[0];
        assertTrue(out.contains("A group with identifier G01 already exists"));
        assertTrue(out.contains("Dragon Rojo"));
        assertTrue(Files.exists(base.resolve("data").resolve("groups.txt")));
    }

    @Test
    void flyRunIsIsolatedAndCleanedUpAfterExit() throws IOException {
        runApp(new String[0], "GROUP CREATE;G01;Normal\nEXIT\n");
        String before = Files.readString(base.resolve("data").resolve("groups.txt"));
        ParkControllerTestSupport.reset();
        Path file = commandFile("fly.txt", "GROUP DELETE;G01\nGROUP CREATE;G02;Fly\nEXIT\n");
        String out = runApp(new String[] {"--fly", file.toString()}, "")[0];
        assertTrue(out.contains("No group with identifier G01"));
        assertTrue(out.contains("GROUP CREATED"));
        assertEquals(0, temporaryDirectories());
        assertEquals(before, Files.readString(base.resolve("data").resolve("groups.txt")));
    }

    @Test
    void interactiveFlyRunCleansUpAtEndOfInput() throws IOException {
        String out = runApp(new String[] {"--fly"}, "HELP\nGROUP CREATE;G05;Temporal\n")[0];
        assertTrue(out.contains("Available commands:"));
        assertEquals(0, temporaryDirectories());
        assertFalse(Files.exists(base.resolve("data").resolve("groups.txt")));
    }

    @Test
    void temporaryDirectoryNamesAreUnique() throws IOException {
        Path first = App.createTemporaryDirectory(base.resolve("tmp"));
        Path second = App.createTemporaryDirectory(base.resolve("tmp"));
        assertTrue(Files.isDirectory(first));
        assertTrue(Files.isDirectory(second));
        assertFalse(first.equals(second));
    }
}
