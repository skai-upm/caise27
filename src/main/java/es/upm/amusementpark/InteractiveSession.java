package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Reads commands line by line from an input source until the end of input.
 */
public class InteractiveSession {

    private final BufferedReader input;

    public InteractiveSession(BufferedReader input) {
        this.input = input;
    }

    /**
     * Consumes input lines until end of input.
     *
     * @return number of lines read
     */
    public int run() {
        int lines = 0;
        try {
            while (input.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return lines;
    }
}
