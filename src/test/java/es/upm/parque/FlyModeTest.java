package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private String runWith(String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, dir, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private long tmpEntries() throws Exception {
        Path tmp = dir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> s = Files.list(tmp)) {
            return s.count();
        }
    }

    @Test
    void flyFileModeUsesTemporaryDataAndCleansUp() throws Exception {
        Path commands = dir.resolve("input_fase2.txt");
        Files.writeString(commands, CREATE_ANA + "\nGROUP CREATE;G01;F\nATTRACTION CREATE;A01;Tornado;20\n"
                + "TICKET CREATE;T001;ana@correo.com;2999-01-01\nUSER CREATE;Ana;X;12345678Z;x@correo.com;Clave1!;6;1234567890123456\nEXIT\n");
        String out = runWith("--fly", commands.toString());
        assertTrue(out.contains("OK USER CREATED"), out);
        assertTrue(out.contains("ERROR a user with that DNI already exists"), out);
        assertFalse(Files.exists(dir.resolve("data")), "normal data must not be touched");
        assertTrue(Files.exists(dir.resolve("tmp")));
        assertTrue(tmpEntries() == 0, "temporary directory must be deleted");
    }

    @Test
    void flyDoesNotSeeOrModifyPermanentData() throws Exception {
        Path first = dir.resolve("first.txt");
        Files.writeString(first, CREATE_ANA + "\n");
        runWith(first.toString());
        String before = Files.readString(dir.resolve("data/users.txt"));
        Path second = dir.resolve("second.txt");
        Files.writeString(second, "USER DELETE;ana@correo.com;Clave1!\n");
        String out = runWith("--fly", second.toString());
        assertTrue(out.startsWith("ERROR"), out);
        assertTrue(before.equals(Files.readString(dir.resolve("data/users.txt"))));
    }

    @Test
    void flyInteractiveModeCleansUp() throws Exception {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream((CREATE_ANA + "\nEXIT\n").getBytes(StandardCharsets.UTF_8)));
            String out = runWith("--fly");
            assertTrue(out.contains("OK USER CREATED"), out);
        } finally {
            System.setIn(original);
        }
        assertFalse(Files.exists(dir.resolve("data")));
        assertTrue(tmpEntries() == 0);
    }

    @Test
    void temporaryDirectoryNamesAreUniqueTimestamps() throws Exception {
        Path a = App.createTemporaryDirectory(dir.resolve("tmp"));
        Path b = App.createTemporaryDirectory(dir.resolve("tmp"));
        assertFalse(a.equals(b));
        assertTrue(a.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
    }
}
