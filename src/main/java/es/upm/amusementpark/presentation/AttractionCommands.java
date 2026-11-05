package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.Attraction;
import es.upm.amusementpark.business.AttractionType;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import es.upm.amusementpark.business.Pass;
import java.io.PrintStream;

/**
 * {@code ATTRACTION CREATE/LIST/ENTER/EXIT/STATUS} commands.
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
        String usage = "ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>[;<Type>]";
        String[] f = CommandSupport.fields(line, line.split(";", -1).length == 5 ? 5 : 4, usage);
        int capacity = parseCapacity(f[3]);
        if (capacity <= 0) {
            throw new InvalidCommandException("Capacity must be a positive whole number");
        }
        AttractionType type = f.length == 5 ? AttractionType.parse(f[4]) : AttractionType.GENERAL;
        if (type == null) {
            throw new InvalidCommandException("Invalid attraction type. Expected GENERAL, INDIVIDUAL, GROUP or SPECIAL");
        }
        park.createAttraction(f[1], f[2], capacity, type);
        out.println("ATTRACTION CREATED");
        return true;
    }

    private Attraction<?> existing(String id) {
        Attraction<?> attraction = park.findAttraction(id);
        if (attraction == null) {
            throw new ParkException("Attraction " + id + " does not exist");
        }
        return attraction;
    }

    private boolean list(String line) {
        String[] f = CommandSupport.fields(line, 2, "ATTRACTION LIST;<AttractionIdentifier>");
        Attraction<?> attraction = existing(f[1]);
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
        String usage = "ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>[;SPEED]";
        String[] f = CommandSupport.fields(line, line.split(";", -1).length == 4 ? 4 : 3, usage);
        if (f.length == 4 && !"SPEED".equals(f[3])) {
            throw new InvalidCommandException("Invalid syntax. Usage: " + usage);
        }
        Attraction.Admission admission = park.enterAttraction(f[1], f[2], f.length == 4);
        out.println("ENTERED ATTRACTION " + f[1] + ": " + String.join(", ", admission.holders()));
        for (Pass pass : admission.toConsume()) {
            out.println("CONSUMED PASS: " + pass.getTypeName() + " " + pass.getId());
        }
        return true;
    }

    private boolean exit(String line) {
        String[] f = CommandSupport.fields(line, 3, "ATTRACTION EXIT;<AttractionIdentifier>;<Email>");
        park.leaveAttraction(f[1], f[2]);
        out.println("USER " + f[2] + " LEFT ATTRACTION " + f[1]);
        return true;
    }

    private boolean status(String line) {
        String[] f = CommandSupport.fields(line, 2, "ATTRACTION STATUS;<AttractionIdentifier>");
        Attraction<?> attraction = existing(f[1]);
        out.println("ATTRACTION " + attraction.getId());
        out.println("  NAME: " + attraction.getName());
        out.println("  TYPE: " + attraction.getType());
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
