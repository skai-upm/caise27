package es.upm.park.persistence;

import java.nio.file.Path;

/**
 * Locations of the park data files. Normal and temporary (--fly) execution
 * differ only in the directory passed here.
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
}
