package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {

    private static final List<String> SESSION = List.of(ParkTestSupport.PEDRO, "SYSTEM DATE;2026-10-01",
            "GROUP CREATE;G01;Familia", "GROUP ADD;G01;pedro@correo.com", "TICKET CREATE;T001;pedro@correo.com;2026-10-15",
            "GROUPTICKET CREATE;TG01;G01;2026-10-20", "ATTRACTION CREATE;A01;Tornado;20", "HELP", "EXIT");

    @TempDir
    Path dir;

    private String runFile(Path workDir, List<String> commands, String... flags) throws IOException {
        Path input = dir.resolve("input_" + System.nanoTime() + ".txt");
        Files.write(input, commands, StandardCharsets.UTF_8);
        String[] args = Stream.concat(Stream.of(flags), Stream.of(input.toString())).toArray(String[]::new);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, workDir, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static boolean tmpIsEmpty(Path workDir) throws IOException {
        Path tmp = workDir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return true;
        }
        try (Stream<Path> entries = Files.list(tmp)) {
            return entries.findAny().isEmpty();
        }
    }

    @Test
    void flyRunDoesNotTouchPermanentDataAndCleansUp() throws IOException {
        Path work = dir.resolve("work");
        Files.createDirectories(work);
        runFile(work, List.of(ParkTestSupport.ANA, "EXIT"));
        byte[] before = Files.readAllBytes(work.resolve("users.txt"));

        String out = runFile(work, SESSION, "--fly");
        assertFalse(out.contains("ERROR"), out);
        assertArrayEquals(before, Files.readAllBytes(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("groups.txt")));
        assertFalse(Files.exists(work.resolve("tickets.txt")));
        assertFalse(Files.exists(work.resolve("attractions.txt")));
        assertTrue(tmpIsEmpty(work));

        String normal = runFile(work, List.of("USER DELETE;pedro@correo.com;Otra2#"));
        assertTrue(normal.contains("ERROR"));
    }

    @Test
    void flyStartsFromEmptyStateAndBehavesLikeNormalMode() throws IOException {
        Path normalWork = dir.resolve("normal");
        Path flyWork = dir.resolve("fly");
        Files.createDirectories(normalWork);
        Files.createDirectories(flyWork);
        runFile(flyWork, List.of(ParkTestSupport.ANA));
        String normal = runFile(normalWork, SESSION);
        String fly = runFile(flyWork, SESSION, "--fly");
        assertEquals(normal, fly);
        assertTrue(Files.exists(normalWork.resolve("grouptickets.txt")));
        assertTrue(tmpIsEmpty(flyWork));
    }

    @Test
    void flyCleansUpAtEndOfFileWithoutExit() throws IOException {
        runFile(dir, List.of(ParkTestSupport.ANA), "--fly");
        assertTrue(tmpIsEmpty(dir));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void flyFlagAfterFileIsAccepted() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of(ParkTestSupport.ANA));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {input.toString(), "--fly"}, dir, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("OK"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void interactiveFlyMode() throws IOException {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream((ParkTestSupport.ANA + "\nEXIT\n").getBytes(StandardCharsets.UTF_8)));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            App.run(new String[] {"--fly"}, dir, new PrintStream(buffer, true, StandardCharsets.UTF_8));
            assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("OK: user ana@correo.com created"));
        } finally {
            System.setIn(original);
        }
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertTrue(tmpIsEmpty(dir));
    }

    @Test
    void flyWithMissingFileStillCleansUp() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {"--fly", dir.resolve("nope.txt").toString()}, dir,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("ERROR: cannot open command file"));
        assertTrue(tmpIsEmpty(dir));
    }

    @Test
    void temporaryDirectoriesAreUniqueAndTimestamped() throws IOException {
        StorageLocation a = StorageLocation.createTemporary(dir.resolve("tmp"));
        StorageLocation b = StorageLocation.createTemporary(dir.resolve("tmp"));
        assertFalse(a.getBaseDir().equals(b.getBaseDir()));
        assertTrue(a.getBaseDir().getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        assertEquals(a.getBaseDir().resolve("groups.txt"), a.groupsFile());
        a.deleteIfTemporary();
        b.deleteIfTemporary();
        b.deleteIfTemporary();
        assertTrue(tmpIsEmpty(dir));
        StorageLocation permanent = new StorageLocation(dir);
        permanent.deleteIfTemporary();
        assertTrue(Files.exists(dir));
    }
}
