package es.upm.amusementpark.presentation;

import es.upm.amusementpark.persistence.TemporaryStorage;

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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {

    private static final String SCRIPT = String.join("\n",
            "SYSTEM DATE;2026-10-01",
            ParkTestSupport.ANA,
            ParkTestSupport.PEDRO,
            "GROUP CREATE;G01;Familia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2026-10-01",
            "GROUPTICKET CREATE;TG01;G01;2026-10-01",
            "ATTRACTION CREATE;A01;Tornado;20",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION STATUS;A01",
            "EXIT") + "\n";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(Path dataDir, String stdin, String... args) {
        out.reset();
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), dataDir);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private long runDirectories() throws IOException {
        Path tmp = dir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> entries = Files.list(tmp)) {
            return entries.count();
        }
    }

    @Test
    void flyRunLeavesPermanentDataUntouchedAndCleansUp() throws IOException {
        run(dir, ParkTestSupport.ANA + "\nEXIT\n");
        List<String> permanent = Files.readAllLines(dir.resolve("users.txt"));

        assertEquals(0, run(dir, SCRIPT.replace(ParkTestSupport.ANA + "\n", "")
                + "USER DELETE;ana@correo.com;Clave1!\n", App.FLY));

        assertEquals(permanent, Files.readAllLines(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
        assertFalse(Files.exists(dir.resolve("tickets.txt")));
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
        assertEquals(0, runDirectories());
    }

    @Test
    void flyStartsFromEmptyStorage() {
        run(dir, ParkTestSupport.ANA + "\n");
        run(dir, ParkTestSupport.ANA + "\n", App.FLY);
        assertTrue(output().startsWith("OK"));
    }

    @Test
    void flyAndNormalRunsBehaveTheSame() {
        run(dir.resolve("normal"), SCRIPT);
        String normal = output();
        run(dir.resolve("fly"), SCRIPT, App.FLY);
        assertEquals(normal, output());
        assertTrue(normal.contains("OK GROUP TICKET CREATED -> 80.00"));
        assertTrue(normal.contains("Users present: 2"));
    }

    @Test
    void flyWorksWithCommandFile() throws IOException {
        Path commands = dir.resolve("input_fase2.txt");
        Files.writeString(commands, SCRIPT);
        assertEquals(0, run(dir, "", App.FLY, commands.toString()));
        assertTrue(output().contains("OK TICKET CREATED -> 40.00"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertEquals(0, runDirectories());
    }

    @Test
    void flyCleansUpAtEndOfInputAndOnUnreadableFile() throws IOException {
        run(dir, ParkTestSupport.ANA + "\n", App.FLY);
        assertEquals(0, runDirectories());
        assertEquals(1, run(dir, "", App.FLY, dir.resolve("missing.txt").toString()));
        assertEquals(0, runDirectories());
    }

    @Test
    void runDirectoriesAreUniqueAndTimestamped() throws IOException {
        Path first = TemporaryStorage.create(dir.resolve("tmp"));
        Path second = TemporaryStorage.create(dir.resolve("tmp"));
        assertFalse(first.equals(second));
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
    }
}
