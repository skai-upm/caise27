package es.upm.amusementpark.park;

import es.upm.amusementpark.cli.CommandProcessor;
import java.io.PrintStream;

/**
 * {@code ATTRACTION CREATE} and {@code ATTRACTION LIST} commands.
 */
public final class AttractionCommands {

    private final ParkController park;
    private final PrintStream out;

    private AttractionCommands(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        AttractionCommands commands = new AttractionCommands(park, out);
        processor.register("ATTRACTION CREATE", commands::create);
        processor.register("ATTRACTION LIST", commands::list);
        processor.register("ATTRACTION ENTER", commands::enter);
        processor.register("ATTRACTION EXIT", commands::exit);
        processor.register("ATTRACTION STATUS", commands::status);
    }

    private boolean create(String line) {
        String[] f = TicketCommands.fields(line);
        if (f.length != 4) {
            out.println("ERROR: Invalid syntax. Usage: ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>");
            return true;
        }
        int capacity = parseCapacity(f[3]);
        if (capacity <= 0) {
            out.println("ERROR: Capacity must be a positive whole number");
            return true;
        }
        try {
            park.createAttraction(f[1], f[2], capacity);
            out.println("ATTRACTION CREATED");
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean list(String line) {
        String[] f = TicketCommands.fields(line);
        if (f.length != 2) {
            out.println("ERROR: Invalid syntax. Usage: ATTRACTION LIST;<AttractionIdentifier>");
            return true;
        }
        Attraction attraction = park.findAttraction(f[1]);
        if (attraction == null) {
            out.println("ERROR: Attraction " + f[1] + " does not exist");
            return true;
        }
        if (attraction.getOccupants().isEmpty()) {
            out.println("ATTRACTION " + attraction.getId() + " USERS: (none)");
            return true;
        }
        out.println("ATTRACTION " + attraction.getId() + " USERS:");
        for (String email : attraction.getOccupants()) {
            out.println("  " + email);
        }
        return true;
    }

    private boolean enter(String line) {
        String[] f = TicketCommands.fields(line);
        if (f.length != 3) {
            out.println("ERROR: Invalid syntax. Usage: ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>");
            return true;
        }
        try {
            out.println("ENTERED ATTRACTION " + f[1] + ": " + String.join(", ", park.enterAttraction(f[1], f[2])));
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean exit(String line) {
        String[] f = TicketCommands.fields(line);
        if (f.length != 3) {
            out.println("ERROR: Invalid syntax. Usage: ATTRACTION EXIT;<AttractionIdentifier>;<Email>");
            return true;
        }
        try {
            park.leaveAttraction(f[1], f[2]);
            out.println("USER " + f[2] + " LEFT ATTRACTION " + f[1]);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean status(String line) {
        String[] f = TicketCommands.fields(line);
        if (f.length != 2) {
            out.println("ERROR: Invalid syntax. Usage: ATTRACTION STATUS;<AttractionIdentifier>");
            return true;
        }
        Attraction attraction = park.findAttraction(f[1]);
        if (attraction == null) {
            out.println("ERROR: Attraction " + f[1] + " does not exist");
            return true;
        }
        out.println("ATTRACTION " + attraction.getId());
        out.println("  NAME: " + attraction.getName());
        out.println("  CAPACITY: " + attraction.getCapacity());
        out.println("  INSIDE: " + attraction.getOccupants().size());
        out.println("  AVAILABLE: " + attraction.getAvailablePlaces());
        if (attraction.getOccupants().isEmpty()) {
            out.println("  USERS: (none)");
        } else {
            out.println("  USERS:");
            for (String email : attraction.getOccupants()) {
                out.println("    " + email);
            }
        }
        return true;
    }

    /** @return the capacity, or {@code -1} if not a positive whole number */
    static int parseCapacity(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < '0' || text.charAt(i) > '9') {
                return -1;
            }
        }
        int value = Integer.parseInt(text);
        return value > 0 ? value : -1;
    }
}
