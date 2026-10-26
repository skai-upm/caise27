package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String SETUP = String.join("\n", ANA, "SYSTEM DATE;2026-10-15",
            "TICKET CREATE;T001;ana@correo.com;2026-10-15", "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com",
            "GROUPTICKET CREATE;TG01;G01;2026-10-20", "ATTRACTION CREATE;A01;Tornado;10",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");

    @TempDir
    Path base;

    /** Standard input that records the files under base/tmp when the application reaches its end. */
    private final class ObservingInput extends InputStream {
        private final InputStream delegate;
        private final List<String> seen = new ArrayList<>();

        ObservingInput(String text) {
            delegate = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b < 0 && seen.isEmpty()) {
                seen.addAll(listTmp());
            }
            return b;
        }
    }

    private List<String> listTmp() {
        Path tmp = base.resolve("tmp");
        if (!Files.exists(tmp)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(tmp)) {
            return walk.filter(Files::isRegularFile).map(p -> base.relativize(p).toString().replace('\\', '/')).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String execute(String[] args, InputStream in) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.execute(args, base, TestPark.CLOCK, in, new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private boolean tmpIsEmpty() throws IOException {
        Path tmp = base.resolve("tmp");
        if (!Files.exists(tmp)) {
            return true;
        }
        try (Stream<Path> entries = Files.list(tmp)) {
            return entries.findAny().isEmpty();
        }
    }

    @Test
    void flyRunUsesOnlyItsTemporaryDirectoryAndRemovesIt() throws IOException {
        execute(new String[0], new ByteArrayInputStream((PEDRO + "\n").getBytes(StandardCharsets.UTF_8)));
        List<String> permanentBefore = Files.readAllLines(base.resolve("data").resolve("users.txt"));

        ObservingInput in = new ObservingInput(SETUP + "\n");
        String out = execute(new String[] {"--fly"}, in);
        assertFalse(out.contains("ERROR"), out);
        assertEquals(List.of("tmp/20260901_100000/attractions.txt", "tmp/20260901_100000/groups.txt",
                "tmp/20260901_100000/grouptickets.txt", "tmp/20260901_100000/tickets.txt",
                "tmp/20260901_100000/users.txt"), in.seen);
        assertTrue(tmpIsEmpty());
        assertEquals(permanentBefore, Files.readAllLines(base.resolve("data").resolve("users.txt")));
        assertFalse(Files.exists(base.resolve("data").resolve("tickets.txt")));
    }

    @Test
    void flyRunStartsEmpty() {
        execute(new String[0], new ByteArrayInputStream((ANA + "\n").getBytes(StandardCharsets.UTF_8)));
        String out = execute(new String[] {"--fly"},
                new ByteArrayInputStream("USER DELETE;ana@correo.com;Clave1!\n".getBytes(StandardCharsets.UTF_8)));
        assertTrue(out.startsWith("ERROR No user"), out);
    }

    @Test
    void directoryRemovedAfterExitAndAfterCommandFile() throws IOException {
        execute(new String[] {"--fly"}, new ByteArrayInputStream((ANA + "\nEXIT\n" + PEDRO + "\n")
                .getBytes(StandardCharsets.UTF_8)));
        assertTrue(tmpIsEmpty());

        Path input = base.resolve("input_fase2.txt");
        Files.write(input, List.of(ANA, "", "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria", "EXIT"));
        String out = execute(new String[] {"--fly", input.toString()}, new ByteArrayInputStream(new byte[0]));
        assertEquals("OK USER CREATED\nOK USER UPDATED\nOK EXIT\n", out);
        assertTrue(tmpIsEmpty());
        assertFalse(Files.exists(base.resolve("data")));
    }

    @Test
    void directoryRemovedAfterInvalidFile() throws IOException {
        String out = execute(new String[] {"--fly", base.resolve("missing.txt").toString()},
                new ByteArrayInputStream(new byte[0]));
        assertTrue(out.startsWith("ERROR Input file not found"));
        assertTrue(tmpIsEmpty());
    }

    @Test
    void normalModePersistsAndCreatesNoTemporaryDirectory() throws IOException {
        execute(new String[0], new ByteArrayInputStream((ANA + "\n").getBytes(StandardCharsets.UTF_8)));
        assertFalse(Files.exists(base.resolve("tmp")));
        assertEquals(1, Files.readAllLines(base.resolve("data").resolve("users.txt")).size());
        String out = execute(new String[0],
                new ByteArrayInputStream("USER DELETE;ana@correo.com;Clave1!\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("OK USER DELETED\n", out);
    }
}
