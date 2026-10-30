package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Feeds lines from any source to the command processor, so interactive and file mode behave identically.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes lines in order until EXIT is executed or the input ends. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            CommandProcessor.Result result = processor.process(line);
            out.println(result.output());
            if (result.exit()) {
                return;
            }
        }
    }

    /**
     * Processes the commands contained in a text file.
     *
     * @return false if the file does not exist or cannot be read; the problem has already been reported
     */
    public boolean runFile(Path commandFile) {
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: cannot read command file " + commandFile);
            return false;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader);
            return true;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: cannot read command file " + commandFile + ": " + e.getMessage());
            return false;
        }
    }
}
