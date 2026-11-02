package es.upm.etsisi.amusementpark;

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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private int run(String stdin, String... args) {
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), out, dir);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(CREATE_ANA + "\nEXIT\n"));
        assertTrue(output().startsWith("USER CREATED"));
    }

    @Test
    void fileModeReadsFirstArgument() throws IOException {
        Path input = Files.write(dir.resolve("input.txt"), List.of(CREATE_ANA, "EXIT"), StandardCharsets.UTF_8);
        assertEquals(0, run("", input.toString()));
        assertEquals(1, Files.readAllLines(dir.resolve("users.txt")).size());
    }

    @Test
    void usersPersistBetweenRuns() {
        run(CREATE_ANA + "\n");
        bytes.reset();
        run(CREATE_ANA + "\n");
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void missingCommandFileFailsInControlledWay() {
        assertEquals(1, run("", dir.resolve("missing.txt").toString()));
        assertTrue(output().startsWith("ERROR: cannot read command file"));
    }

    @Test
    void flyInteractiveLeavesNoDataAndKeepsPermanentData() throws IOException {
        run(CREATE_ANA + "\n");
        List<String> permanent = Files.readAllLines(dir.resolve("users.txt"));
        bytes.reset();

        assertEquals(0, run(CREATE_PEDRO + "\nGROUP CREATE;G01;Familia\nEXIT\n", "--fly"));

        assertTrue(output().startsWith("USER CREATED"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertEquals(permanent, Files.readAllLines(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
    }

    @Test
    void flyStartsFromEmptyState() {
        run(CREATE_ANA + "\n");
        bytes.reset();
        run(CREATE_ANA + "\n", "--fly");
        assertTrue(output().startsWith("USER CREATED"));
    }

    @Test
    void flyFileModeCleansUpAtEndOfFile() throws IOException {
        Path input = Files.write(dir.resolve("input_fase2.txt"),
                List.of(CREATE_ANA, "ATTRACTION CREATE;A01;Dragon Rojo;20"), StandardCharsets.UTF_8);
        assertEquals(0, run("", "--fly", input.toString()));
        assertTrue(output().contains("ATTRACTION CREATED"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void flyCleansUpWhenCommandFileIsMissing() {
        assertEquals(1, run("", "--fly", dir.resolve("missing.txt").toString()));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
