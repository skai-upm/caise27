package es.upm.etsisi.park.persistence;

import java.nio.file.Path;

/**
 * Locations of the data files inside a data directory. Normal and temporary (--fly)
 * executions only differ in the directory.
 */
public record DataPaths(Path directory) {

    public Path users() {
        return directory.resolve("users.txt");
    }

    public Path groups() {
        return directory.resolve("groups.txt");
    }

    public Path tickets() {
        return directory.resolve("tickets.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }

    public Path passes() {
        return directory.resolve("passes.txt");
    }
}
