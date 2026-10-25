package es.upm.parque;

import es.upm.parque.persistence.TemporaryStorage;
import es.upm.parque.persistence.TextUserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FlyModeTest {

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "SYSTEM DATE;2026-10-15",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15",
            "GROUP CREATE;G01;Familia",
            "ATTRACTION CREATE;A01;Tornado;20",
            "EXIT",
            "USER DELETE;ana@correo.com;Clave1!");

    @TempDir
    Path tempDir;

    @Test
    public void temporaryStorageIsUniqueAndDeletedWithItsFiles() throws Exception {
        Path base = tempDir.resolve("tmp");
        TemporaryStorage first = TemporaryStorage.create(base);
        TemporaryStorage second = TemporaryStorage.create(base);
        assertNotEquals(first.getDirectory(), second.getDirectory());
        assertTrue(first.getDirectory().getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));

        ParkApplication.start(first.getDirectory(), ParkTestSupport.FAST_HASHER).process(COMMANDS.get(0));
        assertTrue(Files.exists(first.getDirectory().resolve(TextUserRepository.FILE_NAME)));

        first.delete();
        first.delete();
        assertFalse(Files.exists(first.getDirectory()));
        assertTrue(Files.exists(base), "base is kept while another run uses it");
        second.delete();
        assertFalse(Files.exists(base));
    }

    private String runMain(String input, String... args) {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        try {
            App.main(args);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private void assertNoPersistentTrace(boolean dataExisted, boolean tmpExisted) {
        assertEquals(dataExisted, Files.exists(App.DATA_DIRECTORY), "normal data directory must not be touched");
        assertEquals(tmpExisted, Files.exists(App.TEMPORARY_BASE), "temporary directory must be removed");
    }

    @Test
    public void flyWithCommandFileUsesAndRemovesTemporaryDirectory() throws Exception {
        boolean dataExisted = Files.exists(App.DATA_DIRECTORY);
        boolean tmpExisted = Files.exists(App.TEMPORARY_BASE);
        Path commands = tempDir.resolve("input_fase2.txt");
        Files.write(commands, COMMANDS);

        String output = runMain("", "--fly", commands.toString());

        assertTrue(output.contains("OK TICKET CREATED -> 40.00"), output);
        assertTrue(output.contains("OK ATTRACTION CREATED"), output);
        assertFalse(output.contains("ERROR"), output);
        assertFalse(output.contains("OK USER DELETED"), "nothing runs after EXIT");
        assertNoPersistentTrace(dataExisted, tmpExisted);
    }

    @Test
    public void flyInteractiveUsesAndRemovesTemporaryDirectory() {
        boolean dataExisted = Files.exists(App.DATA_DIRECTORY);
        boolean tmpExisted = Files.exists(App.TEMPORARY_BASE);

        String output = runMain(String.join("\n", COMMANDS.subList(0, 3)) + "\n", "--fly");

        assertTrue(output.contains("OK USER CREATED"), output);
        assertTrue(output.contains("OK TICKET CREATED -> 40.00"), output);
        assertNoPersistentTrace(dataExisted, tmpExisted);
    }

    @Test
    public void flyWithMissingFileStillCleansUp() {
        boolean dataExisted = Files.exists(App.DATA_DIRECTORY);
        boolean tmpExisted = Files.exists(App.TEMPORARY_BASE);

        String output = runMain("", "--fly", tempDir.resolve("no_existe.txt").toString());

        assertTrue(output.contains("ERROR: cannot read command file"), output);
        assertNoPersistentTrace(dataExisted, tmpExisted);
    }
}
