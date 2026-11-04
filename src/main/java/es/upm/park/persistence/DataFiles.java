package es.upm.park.persistence;

import java.nio.file.Path;

/** Locations of the persistence files. */
public record DataFiles(Path users, Path groups, Path tickets, Path groupTickets, Path attractions, Path passes) {

    /** The standard file names inside {@code dir}. */
    public static DataFiles in(Path dir) {
        return new DataFiles(dir.resolve("users.txt"), dir.resolve("groups.txt"), dir.resolve("tickets.txt"),
                dir.resolve("group-tickets.txt"), dir.resolve("attractions.txt"), dir.resolve("passes.txt"));
    }
}
