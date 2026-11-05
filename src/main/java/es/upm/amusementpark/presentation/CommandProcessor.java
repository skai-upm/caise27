package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single command-processing path shared by every input source (standard input or a command file).
 */
public class CommandProcessor {

    /** Basic syntax of the available commands (Delivery 2 list), displayed by {@code HELP}. */
    public static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "  USER DELETE;<Email>;<Password>",
            "  USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "  SYSTEM DATE;<yyyy-MM-dd>",
            "  SYSTEM PRICE;<Price>",
            "  GROUP CREATE;<Identifier>;<Name>",
            "  GROUP ADD;<Identifier>;<Email>",
            "  GROUP REMOVE;<Identifier>;<Email>",
            "  GROUP DELETE;<Identifier>",
            "  TICKET CREATE;<identifier>;<email>;<date>",
            "  TICKET DELETE;<identifier>",
            "  GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "  GROUPTICKET DELETE;<identifier>",
            "  ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "  ATTRACTION ENTER;<attraction>;<ticket>",
            "  ATTRACTION EXIT;<attraction>;<email>",
            "  ATTRACTION LIST;<attraction>",
            "  ATTRACTION STATUS;<attractionIdentifier>",
            "  INVOICE CREATE;USER;<email>",
            "  INVOICE CREATE;GROUP;<groupIdentifier>",
            "  HELP",
            "  EXIT");

    private final PrintStream out;
    private final Map<String, CommandHandler> handlers = new LinkedHashMap<>();
    private Runnable afterCommand = () -> { };

    public CommandProcessor(PrintStream out) {
        this.out = out;
        register("HELP", line -> {
            out.println(HELP_TEXT);
            return true;
        });
        register("EXIT", line -> {
            out.println("Exiting application.");
            return false;
        });
    }

    /**
     * Registers the handler of a command keyword (text before the first {@code ;}).
     *
     * @param keyword command keyword, for example {@code HELP}
     * @param handler handler of the command
     */
    public final void register(String keyword, CommandHandler handler) {
        handlers.put(keyword, handler);
    }

    /**
     * Sets an action run after every processed (non-blank) line, for example to persist state.
     *
     * @param action the action
     */
    public void setAfterCommand(Runnable action) {
        afterCommand = action;
    }

    /**
     * Executes one input line.
     *
     * @param rawLine the line as read from the input source
     * @return {@code false} when the session must stop, {@code true} otherwise
     */
    public boolean execute(String rawLine) {
        String line = rawLine == null ? "" : rawLine.trim();
        if (line.isEmpty()) {
            return true;
        }
        boolean keepGoing = dispatch(line);
        afterCommand.run();
        return keepGoing;
    }


    private boolean dispatch(String line) {
        int separator = line.indexOf(';');
        String keyword = (separator < 0 ? line : line.substring(0, separator)).trim();
        CommandHandler handler = handlers.get(keyword);
        if (handler == null) {
            out.println("ERROR: Unknown command: " + line);
            return true;
        }
        try {
            return handler.handle(line);
        } catch (InvalidCommandException | ParkException e) {
            out.println("ERROR: " + e.getMessage());
            return true;
        } catch (RuntimeException e) {
            out.println("ERROR: Could not process command: " + line);
            return true;
        }
    }

    /**
     * Processes lines in order until {@code EXIT} or the end of the input. Nothing after {@code EXIT} is read.
     *
     * @param input the input source
     * @throws IOException if the input cannot be read
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (!execute(line)) {
                return;
            }
        }
    }
}
