package es.upm.amusementpark.persistence;

import java.nio.file.Path;

/**
 * Locations of the persistent data files inside one data directory. Normal and temporary
 * (--fly) executions differ only in the directory used.
 */
public class DataPaths {

    private final Path directory;

    public DataPaths(Path directory) {
        this.directory = directory;
    }

    public Path directory() {
        return directory;
    }

    public Path users() {
        return directory.resolve("users.txt");
    }

    public Path groups() {
        return directory.resolve("groups.txt");
    }

    public Path tickets() {
        return directory.resolve("tickets.txt");
    }

    public Path passes() {
        return directory.resolve("passes.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }
}
