package es.upm.park.persistence;

import java.nio.file.Path;

/**
 * Configures where repositories keep their files. Normal and temporary (--fly)
 * persistence differ only in the directory given here.
 */
public record StoragePaths(Path directory) {

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
