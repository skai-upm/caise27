package com.amusementpark.presentation;

import com.amusementpark.persistence.TemporaryStorage;

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

    private static final List<String> SCRIPT = List.of(
            CommandProcessorTest.CREATE_PEDRO,
            "SYSTEM DATE;2030-01-10",
            "GROUP CREATE;G01;Familia",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;pedro@correo.com;2030-01-10",
            "GROUPTICKET CREATE;TG01;G01;2030-01-10",
            "ATTRACTION CREATE;A01;Tornado;20",
            "ATTRACTION ENTER;A01;T001",
            "ATTRACTION STATUS;A01",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;611111111",
            "EXIT");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private int run(Path workDir, String stdin, String... args) {
        out.reset();
        return App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()),
                workDir);
    }

    private static String snapshot(Path workDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String name : List.of("users.txt", "groups.txt", "tickets.txt", "group_tickets.txt", "attractions.txt")) {
            Path p = workDir.resolve(name);
            sb.append(name).append('=').append(Files.exists(p) ? Files.readString(p) : "<absent>").append('\n');
        }
        return sb.toString();
    }

    private static long runDirectories(Path workDir) throws IOException {
        Path tmp = workDir.resolve("tmp");
        if (!Files.exists(tmp)) {
            return 0;
        }
        try (Stream<Path> s = Files.list(tmp)) {
            return s.count();
        }
    }

    @Test
    void flyRunLeavesPermanentDataUntouchedAndCleansUp() throws IOException {
        run(dir, CommandProcessorTest.CREATE_ANA + "\nEXIT");
        String before = snapshot(dir);

        assertEquals(0, run(dir, String.join("\n", SCRIPT), "--fly"));
        assertFalse(out.toString(StandardCharsets.UTF_8).contains("ERROR: Could not"));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("ERROR: Unknown email"),
                "ana does not exist inside the fly run");
        assertEquals(before, snapshot(dir));
        assertEquals(0, runDirectories(dir));

        run(dir, "USER CREATE;Ana;X;11111111H;ana@correo.com;Clave1!;6;1234567890123456\nEXIT");
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("ERROR:"), "normal run still sees Ana");
    }

    @Test
    void flyOutputMatchesNormalModeAndWorksWithFile() throws IOException {
        Path normalDir = Files.createDirectory(dir.resolve("normal"));
        Path flyDir = Files.createDirectory(dir.resolve("fly"));
        Path commands = dir.resolve("input_fase2.txt");
        Files.write(commands, SCRIPT);

        assertEquals(0, run(normalDir, "", commands.toString()));
        String normalOut = out.toString(StandardCharsets.UTF_8);
        assertEquals(0, run(flyDir, "", "--fly", commands.toString()));
        assertEquals(normalOut, out.toString(StandardCharsets.UTF_8));
        assertEquals(0, run(flyDir, String.join("\n", SCRIPT), "--fly"));
        assertEquals(normalOut, out.toString(StandardCharsets.UTF_8));

        assertTrue(Files.exists(normalDir.resolve("tickets.txt")));
        try (Stream<Path> s = Files.list(flyDir)) {
            assertEquals(List.of(flyDir.resolve("tmp")), s.toList());
        }
        assertEquals(0, runDirectories(flyDir));
    }

    @Test
    void flyDirectoryIsRemovedAtEndOfInputAndOnFileError() throws IOException {
        assertEquals(0, run(dir, CommandProcessorTest.CREATE_ANA, "--fly"));
        assertEquals(0, runDirectories(dir));
        assertEquals(1, run(dir, "", "--fly", dir.resolve("missing.txt").toString()));
        assertEquals(0, runDirectories(dir));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void flyDirectoriesAreTimestampedAndUnique() throws IOException {
        Path root = dir.resolve("tmp");
        Path first = TemporaryStorage.create(root);
        Path second = TemporaryStorage.create(root);
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        assertFalse(first.equals(second));
        TemporaryStorage.delete(root);
        assertFalse(Files.exists(root));
    }
}
