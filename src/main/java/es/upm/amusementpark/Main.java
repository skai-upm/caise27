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
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.persistence.DataPaths;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.persistence.TemporaryDataDirectory;

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

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path baseDir) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String commandFile = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        TemporaryDataDirectory temporary = null;
        try {
            DataPaths paths;
            if (fly) {
                temporary = TemporaryDataDirectory.create(baseDir);
                paths = new DataPaths(temporary.path());
            } else {
                paths = new DataPaths(baseDir);
            }
            ParkController park = ParkController.getInstance();
            park.start(paths);
            return execute(commandFile, new CommandProcessor(park), in, out, err);
        } catch (PersistenceException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } finally {
            if (temporary != null) {
                temporary.delete();
            }
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
