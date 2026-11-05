package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.ParkSession;

/**
 * Application entry point. Commands are read from the file given as first argument or,
 * without arguments, from standard input; both sources share the same processing.
 * A leading {@code --fly} argument selects temporary persistence in a directory deleted at the end.
 */
public class App {

    /** UTF-8 console output on the process's standard output; intentionally never closed. */
    private static final PrintStream CONSOLE =
            new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);

    public static void main(String[] args) throws IOException {
        execute(args, Path.of(""), System.in, CONSOLE);
    }

    /**
     * Runs the application with the given arguments, resolving data files relative to {@code workDir}.
     * With {@code --fly} as first argument, a temporary data directory is used and deleted at the end.
     */
    static void execute(String[] args, Path workDir, InputStream stdin, PrintStream console) throws IOException {
        ParkSession session;
        try {
            session = ParkSession.open(args, workDir);
        } catch (ParkException e) {
            console.println("ERROR: " + e.getMessage());
            return;
        }
        try (session) {
            if (session.getIgnoredUserLines() > 0) {
                console.println("WARNING: " + session.getIgnoredUserLines() + " invalid line(s) in "
                        + ParkSession.USERS_FILE + " were ignored");
            }
            if (session.getIgnoredParkLines() > 0) {
                console.println("WARNING: " + session.getIgnoredParkLines()
                        + " invalid line(s) in the park data files were ignored");
            }
            start(session.getCommandArgs(), new CommandProcessor(HelpConfiguration.standard()), stdin, console);
        }
    }

    /** Selects the command source from the arguments and processes every command from it. */
    static void start(String[] args, CommandProcessor processor, InputStream stdin, PrintStream output)
            throws IOException {
        if (args.length == 0) {
            run(processor, new InputStreamReader(stdin, StandardCharsets.UTF_8), output);
            return;
        }
        String path = args[0];
        try (Reader file = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(processor, file, output);
        } catch (IOException e) {
            output.println("ERROR: cannot read command file '" + path + "'");
        }
    }

    static void run(CommandProcessor processor, Reader input, PrintStream output) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            String response = processor.process(line);
            if (response != null) {
                output.println(response);
            }
        }
    }
}
