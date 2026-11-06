package es.upm.park;

import es.upm.park.command.CommandProcessor;
import es.upm.park.command.CommandRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; with an argument they are read from the file at that path.
 */
public class App {

    public static void main(String[] args) {
        run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err);
    }

    static void run(String[] args, BufferedReader stdin, PrintStream out, PrintStream err) {
        CommandRunner runner = new CommandRunner(new CommandProcessor(), out);
        if (args.length == 0) {
            runReporting(runner, stdin, err);
            return;
        }
        try (BufferedReader file = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            runner.run(file);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file: " + args[0]);
        }
    }

    private static void runReporting(CommandRunner runner, BufferedReader input, PrintStream err) {
        try {
            runner.run(input);
        } catch (IOException e) {
            err.println("ERROR: Cannot read input: " + e.getMessage());
        }
    }
}
