package es.upm.etsisi.park;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path work;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(InputStream in, String... args) {
        return App.run(args, work, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private static InputStream stdin(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void normalModeStoresDataInWorkingDirectory() {
        assertEquals(0, run(stdin(CREATE_ANA + "\nEXIT\n")));

        assertTrue(Files.exists(work.resolve("users.txt")));
    }

    @Test
    void flyModeWithFileLeavesNoTraces() throws IOException {
        Path input = work.resolve("input.txt");
        Files.write(input, List.of(CREATE_ANA, "SYSTEM DATE;2027-04-15",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "EXIT"));

        assertEquals(0, run(stdin(""), "--fly", input.toString()));

        assertTrue(out.toString(StandardCharsets.UTF_8).contains("TICKET CREATED"));
        assertFalse(Files.exists(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve(App.TEMP_ROOT)));
    }

    @Test
    void flyModeInteractiveLeavesNoTracesAndKeepsNormalData() throws IOException {
        Files.write(work.resolve("users.txt"), List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));

        assertEquals(0, run(stdin(CREATE_ANA + "\nEXIT\n"), "--fly"));

        assertTrue(out.toString(StandardCharsets.UTF_8).contains("USER CREATED"));
        assertEquals(1, Files.readAllLines(work.resolve("users.txt")).size());
        assertFalse(Files.exists(work.resolve(App.TEMP_ROOT)));
    }

    @Test
    void temporaryDirectoriesAreUnique() throws IOException {
        Path first = App.createTemporaryDirectory(work);
        Path second = App.createTemporaryDirectory(work);

        assertFalse(first.equals(second));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
    }

    @Test
    void missingCommandFileIsReported() {
        assertEquals(1, run(stdin(""), work.resolve("nope.txt").toString()));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("not found"));

        assertEquals(1, run(stdin(""), "--fly", work.resolve("nope.txt").toString()));
        assertFalse(Files.exists(work.resolve(App.TEMP_ROOT)));
    }
}
