package es.upm.etsisi.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Feeds command lines from any source to one {@link CommandProcessor}, in order,
 * skipping blank lines and stopping at EXIT. Interactive and file mode share it.
 */
public class CommandRunner {
    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    public void run(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            CommandResult result = processor.process(line);
            out.println(result.message());
            if (result.exit()) {
                return;
            }
        }
    }

    /**
     * Runs the commands of a text file. Returns false, after printing an error,
     * when the file does not exist or cannot be read.
     */
    public boolean runFile(Path file) {
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            out.println("ERROR: cannot read command file " + file);
            return false;
        }
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            run(in);
            return true;
        } catch (IOException e) {
            out.println("ERROR: cannot read command file " + file + ": " + e.getMessage());
            return false;
        }
    }
}
