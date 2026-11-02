package es.upm.etsisi.amusementpark.park;

import java.nio.file.Path;

/**
 * Locations of the park data files. Normal and --fly mode differ only in the directory.
 */
public record DataPaths(Path users, Path groups, Path tickets, Path attractions) {

    public static DataPaths in(Path directory) {
        return new DataPaths(directory.resolve("users.txt"), directory.resolve("groups.txt"),
                directory.resolve("tickets.txt"), directory.resolve("attractions.txt"));
    }
}
