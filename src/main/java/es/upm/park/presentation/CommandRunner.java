package es.upm.park.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Feeds command lines from an input source to the {@link CommandProcessor}
 * and prints each result. Interactive and file input share the same loop.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes the commands of a text file, one per line.
     */
    public void runFile(String path) {
        try (BufferedReader in = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(in);
        } catch (IOException | InvalidPathException e) {
            out.println("Error: cannot read command file " + path);
        }
    }

    /**
     * Processes commands typed on the given input stream until it ends.
     */
    public void runStandardInput(InputStream input) {
        try {
            run(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            out.println("Error: cannot read commands");
        }
    }

    /**
     * Single processing loop: one command per line, stopping after EXIT.
     */
    void run(BufferedReader in) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = in.readLine()) != null) {
            String result = processor.process(line);
            if (result != null) {
                out.println(result);
            }
        }
    }
}
