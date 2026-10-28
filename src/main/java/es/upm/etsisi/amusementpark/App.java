package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.cli.CommandProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Amusement park application entry point.
 *
 * <p>Without arguments commands are read interactively from standard input;
 * with an argument they are read, in order, from the given command file.
 */
public class App {

    public static void main(String[] args) {
        run(args, System.in, System.out);
    }

    static void run(String[] args, java.io.InputStream stdin, PrintStream out) {
        CommandProcessor processor = new CommandProcessor();
        if (args.length == 0) {
            try {
                processor.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } catch (IOException e) {
                out.println("ERROR: Cannot read standard input");
            }
            return;
        }
        String file = args[0];
        try (BufferedReader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            processor.run(in, out);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot read command file: " + file);
        }
    }
}
