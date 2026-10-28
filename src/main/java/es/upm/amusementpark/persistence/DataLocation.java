package es.upm.amusementpark.persistence;

import java.nio.file.Path;

/**
 * Directory holding the data files of the park. Normal and temporary persistence differ only in
 * this location.
 */
public record DataLocation(Path directory) {

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
