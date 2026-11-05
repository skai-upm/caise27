package es.upm.amusementpark;

import es.upm.amusementpark.cli.CommandProcessor;
import es.upm.amusementpark.park.GroupCommands;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.park.SystemCommands;
import es.upm.amusementpark.park.TicketCommands;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point: reads commands from standard input, or from the file given as first argument.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), System.out);
    }

    /**
     * Runs the application with an explicit standard input and output (testable entry point).
     *
     * @param args command-line arguments; the first one, if present, is the command file path
     * @param stdin reader used when no file argument is given
     * @param out output stream
     */
    public static void run(String[] args, BufferedReader stdin, PrintStream out) {
        ParkController park = ParkController.getInstance();
        park.reset();
        CommandProcessor processor = new CommandProcessor(out);
        SystemCommands.register(processor, park, out);
        GroupCommands.register(processor, park, out);
        TicketCommands.register(processor, park, out);
        if (args.length == 0) {
            process(processor, stdin, out, "standard input");
            return;
        }
        try (BufferedReader file = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            process(processor, file, out, args[0]);
        } catch (IOException | RuntimeException e) {
            out.println("ERROR: Cannot read command file: " + args[0]);
        }
    }

    private static void process(CommandProcessor processor, BufferedReader input, PrintStream out, String source) {
        try {
            processor.run(input);
        } catch (IOException e) {
            out.println("ERROR: Cannot read commands from " + source);
        }
    }
}
