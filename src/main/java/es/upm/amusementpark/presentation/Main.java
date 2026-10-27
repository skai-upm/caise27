package es.upm.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.amusementpark.business.controller.ApplicationInitializer;
import es.upm.amusementpark.persistence.PersistenceException;

/**
 * Application entry point. Launch forms: no arguments (interactive), {@code <file>},
 * {@code --fly} and {@code --fly <file>}; --fly keeps all data in a temporary directory
 * that is deleted when the execution ends.
 */
public class Main {

    private static final String FLY = "--fly";

    public static void main(String[] args) {
        int code = run(args, System.in, System.out, System.err, Path.of("."));
        if (code != 0) {
            System.exit(code);
        }
    }

    public static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path baseDir) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String commandFile = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        try {
            if (fly) {
                ApplicationInitializer.startTemporary(baseDir);
            } else {
                ApplicationInitializer.startPermanent(baseDir);
            }
            return execute(commandFile, new CommandProcessor(), in, out, err);
        } catch (PersistenceException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            ApplicationInitializer.close();
        }
    }

    private static int execute(String commandFile, CommandProcessor processor, InputStream in, PrintStream out,
            PrintStream err) {
        if (commandFile == null) {
            return process(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), processor, out, err);
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            return process(reader, processor, out, err);
        } catch (IOException e) {
            err.println("Error: cannot read file " + commandFile);
            return 1;
        }
    }

    private static int process(BufferedReader reader, CommandProcessor processor, PrintStream out, PrintStream err) {
        try {
            String line;
            while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
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
