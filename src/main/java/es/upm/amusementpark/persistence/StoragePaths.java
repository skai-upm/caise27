package es.upm.amusementpark.persistence;

import java.nio.file.Path;

/**
 * Locations of every persistence file. Normal and {@code --fly} executions
 * differ only in the directory given here.
 */
public record StoragePaths(Path directory) {

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

    public Path passes() {
        return directory.resolve("passes.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }
}
