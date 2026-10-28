package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads commands one per line from any line source (standard input or a file)
 * and executes them until EXIT is processed or the input ends.
 */
public class CommandProcessor {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private Runnable afterCommand = () -> { };

    /**
     * Wires the commands with the singleton controllers.
     */
    public CommandProcessor() {
        this(ParkController.getInstance(), InvoiceController.getInstance());
    }

    /**
     * Creates the processor with the controllers its commands collaborate with.
     */
    public CommandProcessor(ParkController park, InvoiceController invoices) {
        register("SYSTEM DATE", new SystemDateCommand(park));
        register("SYSTEM PRICE", new SystemPriceCommand(park));
        register("GROUP CREATE", new GroupCreateCommand(park));
        register("GROUP ADD", new GroupAddCommand(park));
        register("GROUP REMOVE", new GroupRemoveCommand(park));
        register("GROUP DELETE", new GroupDeleteCommand(park));
        register("TICKET CREATE", new TicketCreateCommand(park));
        register("TICKET DELETE", new TicketDeleteCommand(park));
        register("GROUPTICKET CREATE", new GroupTicketCreateCommand(park));
        register("GROUPTICKET DELETE", new GroupTicketDeleteCommand(park));
        register("ATTRACTION CREATE", new AttractionCreateCommand(park));
        register("ATTRACTION LIST", new AttractionListCommand(park));
        register("ATTRACTION ENTER", new AttractionEnterCommand(park));
        register("ATTRACTION EXIT", new AttractionExitCommand(park));
        register("ATTRACTION STATUS", new AttractionStatusCommand(park));
        register("INVOICE CREATE", new InvoiceCreateCommand(invoices));
        register("HELP", new HelpCommand());
        register("EXIT", new ExitCommand());
    }

    public final void register(String keyword, Command command) {
        commands.put(keyword, command);
    }

    /**
     * Sets an action executed after every processed command (e.g. saving the park state).
     */
    public void setAfterCommand(Runnable afterCommand) {
        this.afterCommand = afterCommand;
    }

    /**
     * Processes the lines of {@code in}. No line after an EXIT command is read.
     */
    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            boolean stop = process(line, out);
            if (!line.isBlank()) {
                afterCommand.run();
            }
            if (stop) {
                return;
            }
        }
    }

    /**
     * Processes a single command line.
     *
     * @return {@code true} if processing must stop
     */
    public boolean process(String line, PrintStream out) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String[] fields = trimmed.split(";", -1);
        Command command = commands.get(fields[0].trim());
        if (command == null) {
            out.println("ERROR: Unknown command: " + trimmed);
            return false;
        }
        try {
            return command.execute(Arrays.copyOfRange(fields, 1, fields.length), out);
        } catch (RuntimeException e) {
            out.println("ERROR: Could not execute command: " + trimmed
                    + (e.getMessage() == null ? "" : " (" + e.getMessage() + ")"));
            return false;
        }
    }
}
