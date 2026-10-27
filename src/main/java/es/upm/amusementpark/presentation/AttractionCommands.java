package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.User;

/**
 * ATTRACTION commands.
 */
class AttractionCommands {

    private static final String SYNTAX = "ATTRACTION CREATE;Identifier;Name;Capacity[;Type]";

    private final AttractionController attractions;

    AttractionCommands(AttractionController attractions) {
        this.attractions = attractions;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        AttractionType type = AttractionType.GENERAL;
        if (in.size() == 5) {
            in.expect(4, SYNTAX);
            type = AttractionType.fromText(in.get(4));
            if (type == null) {
                throw new InvalidCommandException("Invalid attraction type. Use GENERAL, INDIVIDUAL, GROUP or SPECIAL");
            }
        } else {
            in.expect(3, SYNTAX);
        }
        int capacity = in.number(3, "Capacity must be a positive whole number");
        attractions.create(in.get(1), in.get(2), capacity, type);
        return "ATTRACTION CREATED";
    }

    String enter(CommandInput in) throws InvalidCommandException, ParkException {
        String syntax = "ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier[;SPEED]";
        boolean speed = in.size() == 4;
        in.expect(speed ? 3 : 2, syntax);
        if (speed && !in.get(3).equals("SPEED")) {
            throw new InvalidCommandException("Invalid format. Use " + syntax);
        }
        EntryResult entry = attractions.enter(in.get(1), in.get(2), speed);
        StringBuilder out = new StringBuilder("ENTERED " + in.get(1) + ":");
        for (User user : entry.users()) {
            out.append(' ').append(user.getEmail());
        }
        for (Pass pass : entry.consumedPasses()) {
            out.append(" (consumed ").append(pass instanceof SpecialPass ? "Special Pass " : "SpeedPass ")
                    .append(pass.getId()).append(')');
        }
        return out.toString();
    }

    String exit(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "ATTRACTION EXIT;AttractionIdentifier;Email");
        User user = attractions.exit(in.get(1), in.get(2));
        return "EXITED " + in.get(1) + ": " + user.getEmail();
    }

    String list(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "ATTRACTION LIST;AttractionIdentifier");
        Attraction<?> attraction = attractions.find(in.get(1));
        if (attraction.occupancy() == 0) {
            return "No users in " + attraction.getId();
        }
        StringBuilder out = new StringBuilder("Users in " + attraction.getId() + ":");
        for (User user : attraction.getOccupants()) {
            out.append('\n').append(user.getEmail());
        }
        return out.toString();
    }

    String status(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "ATTRACTION STATUS;AttractionIdentifier");
        Attraction<?> attraction = attractions.find(in.get(1));
        StringBuilder users = new StringBuilder();
        for (User user : attraction.getOccupants()) {
            users.append(users.length() == 0 ? "" : ", ").append(user.getEmail());
        }
        return String.join("\n",
                "Attraction: " + attraction.getId(),
                "Name: " + attraction.getName(),
                "Type: " + attraction.getType(),
                "Capacity: " + attraction.getCapacity(),
                "Users inside: " + attraction.occupancy(),
                "Available places: " + attraction.freePlaces(),
                "Users: " + (users.length() == 0 ? "(none)" : users));
    }
}
