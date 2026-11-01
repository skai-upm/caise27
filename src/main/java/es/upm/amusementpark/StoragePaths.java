package es.upm.amusementpark;

import java.nio.file.Path;

/**
 * Locations of the persistence files. Normal and --fly executions differ only
 * in the directory given here.
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

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }
}
