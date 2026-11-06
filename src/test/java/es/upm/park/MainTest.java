package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MainTest {

    private String run(String input, String... args) {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Main.main(args);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
    }

    @Test
    void startsWithoutArguments() {
        assertEquals(Main.BANNER, run(""));
    }

    @Test
    void startsWithFileArgument() {
        assertEquals(Main.BANNER, run("", "input.txt"));
    }

    @Test
    void processesStandardInputInOrder() {
        String input = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "\n"
                + "FOO BAR;1\n"
                + "USER CREATE;Luis;Perez;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444\n";
        String[] lines = run(input).split("\n");
        assertEquals(4, lines.length);
        assertEquals(Main.BANNER, lines[0]);
        assertEquals("User created: 12345678Z", lines[1]);
        assertEquals("Error: unknown command 'FOO BAR'", lines[2]);
        assertEquals("Error: a user with DNI 12345678Z already exists", lines[3]);
    }
}
