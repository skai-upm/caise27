package es.upm.etsisi.amusementpark.persistence;

import java.nio.file.Path;

/**
 * Location of the persistent data files. Normal and --fly runs differ only in the directory.
 */
public record Storage(Path dir) {

    public Path usersFile() {
        return dir.resolve("users.txt");
    }

    public Path groupsFile() {
        return dir.resolve("groups.txt");
    }

    public Path ticketsFile() {
        return dir.resolve("tickets.txt");
    }

    public Path attractionsFile() {
        return dir.resolve("attractions.txt");
    }

    public Path passesFile() {
        return dir.resolve("passes.txt");
    }
}
