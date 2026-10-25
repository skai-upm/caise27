package es.upm.parque;

import java.nio.file.Path;

/**
 * Locations of every persistence file, all inside one directory. Changing the directory is the only
 * difference between normal and --fly executions.
 */
public class StoragePaths {

    private final Path directory;

    public StoragePaths(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
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

    public Path groupTickets() {
        return directory.resolve("grouptickets.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }
}
