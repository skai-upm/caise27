package es.upm.parque.persistence;

import java.nio.file.Path;

/**
 * Locations of every persistence file inside one data directory (normal data/ or a --fly temporary directory).
 */
public record StorageLocation(Path directory) {

    public Path users() {
        return directory.resolve("users.txt");
    }

    public Path groups() {
        return directory.resolve("groups.txt");
    }

    public Path tickets() {
        return directory.resolve("tickets.txt");
    }

    public Path groupTickets() {
        return directory.resolve("grouptickets.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }

    public Path passes() {
        return directory.resolve("passes.txt");
    }
}
