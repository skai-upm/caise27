package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments it reads commands interactively from
 * standard input; with one argument it reads them from the given text file.
 */
public class Main {

    public static void main(String[] args) {
        int code = run(args, System.in, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, InputStream in, PrintStream err) {
        if (args.length == 0) {
            return process(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), err);
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            return process(reader, err);
        } catch (IOException e) {
            err.println("Error: cannot read file " + args[0]);
            return 1;
        }
    }

    private static int process(BufferedReader reader, PrintStream err) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                handle(line);
            }
            return 0;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private static void handle(String line) {
        // Command processing is introduced by later milestones.
    }
}
