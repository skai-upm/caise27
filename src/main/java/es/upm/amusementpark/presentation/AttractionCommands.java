package es.upm.amusementpark.presentation;

import java.util.List;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.User;

/**
 * ATTRACTION commands.
 */
class AttractionCommands {

    private final AttractionController attractions;

    AttractionCommands(AttractionController attractions) {
        this.attractions = attractions;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(3, "ATTRACTION CREATE;Identifier;Name;Capacity");
        int capacity = in.number(3, "Capacity must be a positive whole number");
        attractions.create(in.get(1), in.get(2), capacity);
        return "ATTRACTION CREATED";
    }

    String enter(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier");
        List<User> entered = attractions.enter(in.get(1), in.get(2));
        StringBuilder out = new StringBuilder("ENTERED " + in.get(1) + ":");
        for (User user : entered) {
            out.append(' ').append(user.getEmail());
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
        Attraction attraction = attractions.find(in.get(1));
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
        Attraction attraction = attractions.find(in.get(1));
        StringBuilder users = new StringBuilder();
        for (User user : attraction.getOccupants()) {
            users.append(users.length() == 0 ? "" : ", ").append(user.getEmail());
        }
        return String.join("\n",
                "Attraction: " + attraction.getId(),
                "Name: " + attraction.getName(),
                "Capacity: " + attraction.getCapacity(),
                "Users inside: " + attraction.occupancy(),
                "Available places: " + attraction.freePlaces(),
                "Users: " + (users.length() == 0 ? "(none)" : users));
    }
}
