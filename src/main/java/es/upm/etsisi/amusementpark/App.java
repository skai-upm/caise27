package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application entry point. Supported forms:
 * <pre>
 * java -jar nombre-aplicacion.jar                    interactive
 * java -jar nombre-aplicacion.jar input.txt          command file
 * java -jar nombre-aplicacion.jar --fly              interactive, temporary data
 * java -jar nombre-aplicacion.jar --fly input.txt    command file, temporary data
 * </pre>
 */
public class App {

    static final Path DATA_DIRECTORY = Path.of(".");
    static final String FLY_OPTION = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, DATA_DIRECTORY);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path dataDirectory) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        String commandFile = args.length > fileArgument ? args[fileArgument] : null;
        if (!fly) {
            return execute(commandFile, in, out, err, dataDirectory);
        }
        FlyWorkspace workspace;
        try {
            workspace = FlyWorkspace.create(dataDirectory.resolve("tmp"), LocalDateTime.now());
        } catch (ParkException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        int status = execute(commandFile, in, out, err, workspace.getDirectory());
        try {
            workspace.close();
        } catch (ParkException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        return status;
    }

    private static int execute(String commandFile, InputStream in, PrintStream out, PrintStream err,
                               Path dataDirectory) {
        ParkController park = ParkController.getInstance();
        try {
            park.initialize(new ParkRepositories(dataDirectory, err), LocalDate.now());
        } catch (ParkException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(park, out);
        try {
            if (commandFile == null) {
                processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } else {
                Path path = Path.of(commandFile);
                if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                    err.println("ERROR: cannot read command file " + commandFile);
                    return 1;
                }
                processor.runFile(path);
            }
        } catch (IOException e) {
            err.println("ERROR: failed reading commands: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
