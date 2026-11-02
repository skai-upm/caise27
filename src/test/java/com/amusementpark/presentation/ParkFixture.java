package com.amusementpark.presentation;

import com.amusementpark.business.controller.ParkController;
import com.amusementpark.business.security.BCryptPasswordHasher;
import com.amusementpark.business.security.PasswordHasher;
import com.amusementpark.persistence.StoragePaths;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Test helper: a park on a fixed start date whose commands return their printed output.
 */
class ParkFixture {

    static final LocalDate START = LocalDate.of(2026, 10, 1);

    /** Low-cost BCrypt so tests stay fast. */
    static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    final Path dir;
    ParkController park;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    ParkFixture(Path dir) throws IOException {
        this.dir = dir;
        restart();
    }

    /** Reloads everything from disk, as a new execution would. */
    final void restart() throws IOException {
        park = App.configure(StoragePaths.in(dir), HASHER, START);
        buffer = new ByteArrayOutputStream();
        processor = App.createProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Executes one command and returns what it printed, without the trailing newline. */
    String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).stripTrailing();
    }

    /** Executes commands, failing if any of them does not succeed. */
    void ok(String... commands) {
        for (String c : commands) {
            String result = run(c);
            if (result.startsWith("ERROR")) {
                throw new AssertionError(c + " -> " + result);
            }
        }
    }

    /** Registers a valid user whose email is {@code <name>@correo.com} and password {@code Clave1!}. */
    void user(String name, int number) {
        String digits = String.format("%08d", number);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23);
        ok("USER CREATE;" + name + ";Apellido;" + digits + letter + ";" + name.toLowerCase()
                + "@correo.com;Clave1!;600000000;1234567890123456");
    }

    /** Creates group {@code id} with {@code size} new members named {@code <id>m<n>}. */
    void group(String id, int size, int firstNumber) {
        ok("GROUP CREATE;" + id + ";Grupo " + id);
        for (int i = 0; i < size; i++) {
            String name = id + "m" + i;
            user(name, firstNumber + i);
            ok("GROUP ADD;" + id + ";" + name.toLowerCase() + "@correo.com");
        }
    }
}
