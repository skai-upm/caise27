package es.upm.etsisi.park.support;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import es.upm.etsisi.park.ApplicationContext;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.persistence.text.TextRepositories;

/** Wires the application on text repositories in a test directory, with a fast BCrypt cost. */
public final class TestContext {

    public static final int FAST_ROUNDS = 4;

    private TestContext() {
    }

    public static void initialize(Path dataDirectory) throws Exception {
        ApplicationContext.initialize(TextRepositories.in(dataDirectory, new PrintStream(new ByteArrayOutputStream())),
                new BCryptPasswordHasher(FAST_ROUNDS));
    }
}
