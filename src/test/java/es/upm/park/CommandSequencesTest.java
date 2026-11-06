package es.upm.park;

import es.upm.park.business.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Milestone F1-C07 example: the same command sequence from standard input and from a file. */
class CommandSequencesTest {

    private static final String EXAMPLE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "EXIT",
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave3$;600000000;1111222233334444");

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String[] run(Path dir, String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        App.run(args, dir, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new String[] {out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8)};
    }

    @Test
    void stdinAndFileProduceTheSameOrderedOutput() throws IOException {
        String interactive = run(base.resolve("interactive"), EXAMPLE)[0];
        ParkControllerTestSupport.reset();
        Path file = Files.writeString(base.resolve("input.txt"), EXAMPLE, StandardCharsets.UTF_8);
        String fromFile = run(base.resolve("file"), "", file.toString())[0];
        assertEquals(interactive, fromFile);
        String nl = System.lineSeparator();
        assertEquals("USER CREATED" + nl + "USER CREATED" + nl + "USER UPDATED" + nl + "Exiting the application." + nl,
                fromFile);
    }

    @Test
    void nonexistentFileIsHandledInAControlledWay() {
        String[] result = run(base, "", base.resolve("missing.txt").toString());
        assertEquals("", result[0]);
        assertTrue(result[1].contains("Cannot read command file"));
    }
}
