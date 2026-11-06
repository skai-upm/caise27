package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MainTest {

    private String run(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Main.main(args);
        } finally {
            System.setOut(original);
        }
        return out.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    void startsWithoutArguments() {
        assertEquals(Main.BANNER, run());
    }

    @Test
    void startsWithFileArgument() {
        assertEquals(Main.BANNER, run("input.txt"));
    }
}
