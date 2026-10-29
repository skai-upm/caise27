package es.upm.amusementpark;

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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String INPUT = "HELP\nFOO\nEXIT\nHELP\n";

    @TempDir
    Path base;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        App.run(args, base, in, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Path commandFile(String content) throws IOException {
        Path file = base.resolve("input.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private boolean tmpIsEmpty() throws IOException {
        Path tmp = base.resolve(App.TEMP_DIRECTORY);
        if (!Files.exists(tmp)) {
            return true;
        }
        try (Stream<Path> children = Files.list(tmp)) {
            return children.findAny().isEmpty();
        }
    }

    @Test
    void stdinAndFileProduceIdenticalOutput() throws IOException {
        String fromStdin = run(INPUT);
        String fromFile = run("", commandFile(INPUT).toString());
        assertEquals(fromStdin, fromFile);
    }

    @Test
    void fileModeStopsAtExit() throws IOException {
        String out = run("", commandFile(INPUT).toString());
        assertEquals(1, out.split("Available commands:", -1).length - 1);
        assertTrue(out.contains("Error: unknown command: FOO"));
    }

    @Test
    void stdinModeStopsAtExit() {
        assertFalse(run("EXIT\nHELP\n").contains("Available commands:"));
    }

    @Test
    void missingFileIsHandledInAControlledManner() {
        Path missing = base.resolve("missing.txt");
        String out = run("", missing.toString());
        assertTrue(out.contains("Error: could not read command file: " + missing));
        assertFalse(out.contains("Exception"));
    }

    @Test
    void normalModeReconstructsStateOnRestart() {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20\nGROUP CREATE;G01;Familia Garcia\nEXIT\n");
        assertTrue(Files.exists(base.resolve(App.DATA_DIRECTORY).resolve("attractions.txt")));
        String out = run("ATTRACTION STATUS;A01\nGROUP CREATE;G01;Otra\nEXIT\n");
        assertTrue(out.contains("NAME: Dragon Rojo"));
        assertTrue(out.contains("Error: group already exists: G01"));
    }

    @Test
    void stateSavedWhenFileEndsWithoutExit() throws IOException {
        run("", commandFile("ATTRACTION CREATE;A01;Dragon Rojo;20\n").toString());
        assertTrue(run("ATTRACTION STATUS;A01\n").contains("CAPACITY: 20"));
    }

    @Test
    void flyInteractiveLeavesNoDataAndIsIsolated() throws IOException {
        run("ATTRACTION CREATE;A01;Dragon Rojo;20\nEXIT\n");
        String fly = run("ATTRACTION STATUS;A01\nATTRACTION CREATE;A02;Torre del Cielo;12\nEXIT\n", App.FLY);
        assertTrue(fly.contains("Error: attraction not found: A01"));
        assertTrue(tmpIsEmpty());
        String normal = run("ATTRACTION STATUS;A02\nATTRACTION STATUS;A01\n");
        assertTrue(normal.contains("Error: attraction not found: A02"));
        assertTrue(normal.contains("NAME: Dragon Rojo"));
    }

    @Test
    void flyWithCommandFileCleansUpAtEndOfFile() throws IOException {
        Path file = commandFile("GROUP CREATE;G01;Familia Garcia\nATTRACTION CREATE;A01;Dragon Rojo;20\n");
        String out = run("", App.FLY, file.toString());
        assertTrue(out.contains("GROUP CREATED"));
        assertTrue(tmpIsEmpty());
        assertFalse(Files.exists(base.resolve(App.DATA_DIRECTORY)));
    }

    @Test
    void flyWithMissingFileStillCleansUp() throws IOException {
        String out = run("", App.FLY, base.resolve("missing.txt").toString());
        assertTrue(out.contains("Error: could not read command file"));
        assertTrue(tmpIsEmpty());
    }
}
