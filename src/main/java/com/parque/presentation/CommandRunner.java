package com.parque.presentation;

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
 * Presentation layer: reads commands, one per line, from standard input or from a command file, sends each
 * non-blank line to the same {@link CommandProcessor} and prints its response, stopping after EXIT.
 */
public class CommandRunner {
    private final CommandProcessor processor;
    private final PrintStream out;
    private final PrintStream err;

    public CommandRunner(CommandProcessor processor, PrintStream out, PrintStream err) {
        this.processor = processor;
        this.out = out;
        this.err = err;
    }

    /** Processes standard input until EXIT or end of input; returns the exit status. */
    public int runInteractive(InputStream stdin) {
        try {
            processLines(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            err.println("ERROR: Cannot read standard input: " + describe(e));
            return 1;
        }
        return 0;
    }

    /** Processes the command file until EXIT or its end; a missing/unreadable file is reported with status 1. */
    public int runFile(String commandFile) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            processLines(reader);
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    /** The single command loop shared by interactive and file mode. */
    private void processLines(BufferedReader reader) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            out.println(processor.process(line));
        }
    }

    public static String describe(Exception e) {
        String kind = e.getClass().getSimpleName();
        return e.getMessage() == null ? kind : kind + " (" + e.getMessage() + ")";
    }
}
