package es.upm.park;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private Path dataDir;
    private Path tmpRoot;

    @BeforeEach
    void setUp() throws IOException {
        dataDir = Files.createDirectory(dir.resolve("data"));
        tmpRoot = dir.resolve("tmp");
        System.setProperty(App.DATA_DIR_PROPERTY, dataDir.toString());
        System.setProperty(App.TMP_ROOT_PROPERTY, tmpRoot.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(App.DATA_DIR_PROPERTY);
        System.clearProperty(App.TMP_ROOT_PROPERTY);
    }

    private int run(String[] args, String stdin) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in,
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileModeProcessesCommandsInOrderAndStopsAtExit() throws Exception {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT",
                "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"));

        assertEquals(0, run(new String[]{commands.toString()}, ""));

        String output = output();
        assertTrue(output.startsWith("USER CREATED" + System.lineSeparator() + "USER CREATED"));
        assertTrue(output.contains("USER UPDATED"));
        assertTrue(output.contains("USER DELETED"));
        assertTrue(output.contains("Available commands"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(dataDir.resolve("users.txt")));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        assertEquals(0, run(new String[0], "HELP\nEXIT\n"));
        assertTrue(output().contains("Available commands"));
    }

    @Test
    void usersPersistAcrossRuns() {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        out.reset();
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void missingCommandFileIsReportedInControlledWay() {
        int status = run(new String[]{dir.resolve("missing.txt").toString()}, "");
        assertEquals(1, status);
        String error = err.toString(StandardCharsets.UTF_8);
        assertTrue(error.contains("missing.txt"));
        assertFalse(error.contains("Exception"));
        assertEquals(1, run(new String[]{App.FLY_OPTION, dir.resolve("missing.txt").toString()}, ""));
    }

    @Test
    void directoryAsCommandFileIsRejected() {
        assertEquals(1, run(new String[]{dir.toString()}, ""));
    }

    @Test
    void flyModeWithFileLeavesNoData() throws Exception {
        Path commands = dir.resolve("input_fase2.txt");
        Files.write(commands, List.of(
                "SYSTEM DATE;2027-04-15",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION ENTER;A01;T001"));

        assertEquals(0, run(new String[]{App.FLY_OPTION, commands.toString()}, ""));

        assertTrue(output().contains("ATTRACTION ENTERED: 1 user(s)"));
        assertFalse(Files.exists(tmpRoot), "temporary directory must be removed");
        try (Stream<Path> files = Files.list(dataDir)) {
            assertEquals(0, files.count(), "normal data must be untouched");
        }
    }

    @Test
    void flyModeInteractiveIsIsolatedFromNormalData() throws Exception {
        run(new String[0],
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        List<String> before = Files.readAllLines(dataDir.resolve("users.txt"));
        out.reset();

        run(new String[]{App.FLY_OPTION},
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");

        assertTrue(output().startsWith("USER CREATED"), "fly mode starts from empty data");
        assertEquals(before, Files.readAllLines(dataDir.resolve("users.txt")));
        assertFalse(Files.exists(tmpRoot));
    }

    @Test
    void flyDirectoryKeepsExistingTmpRootAndUsesUniqueNames() throws Exception {
        Files.createDirectories(tmpRoot);
        Path first = App.createFlyDirectory(tmpRoot);
        Path second = App.createFlyDirectory(tmpRoot);
        assertFalse(first.equals(second));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));

        run(new String[]{App.FLY_OPTION}, "EXIT\n");
        try (Stream<Path> entries = Files.list(tmpRoot)) {
            assertEquals(2, entries.count(), "only the directory created by the run is removed");
        }
    }
}
