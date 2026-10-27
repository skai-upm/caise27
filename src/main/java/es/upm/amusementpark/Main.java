package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Application entry point. Without arguments it reads commands interactively from
 * standard input; with one argument it reads them from the given text file.
 */
public class Main {

    public static void main(String[] args) {
        int code = run(args, System.in, System.out, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        CommandProcessor processor = new CommandProcessor(new UserRepository());
        if (args.length == 0) {
            return process(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), processor, out, err);
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            return process(reader, processor, out, err);
        } catch (IOException e) {
            err.println("Error: cannot read file " + args[0]);
            return 1;
        }
    }

    private static int process(BufferedReader reader, CommandProcessor processor, PrintStream out, PrintStream err) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String response = processor.process(line);
                if (response != null) {
                    out.println(response);
                }
            }
            return 0;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
