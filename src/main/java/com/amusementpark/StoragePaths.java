package com.amusementpark;

import java.nio.file.Path;

/**
 * Locations of every persistence file. Normal and --fly runs differ only in the directory used.
 */
public record StoragePaths(Path users, Path groups, Path tickets, Path groupTickets, Path attractions) {

    public static StoragePaths in(Path dir) {
        return new StoragePaths(
                dir.resolve("users.txt"),
                dir.resolve("groups.txt"),
                dir.resolve("tickets.txt"),
                dir.resolve("group_tickets.txt"),
                dir.resolve("attractions.txt"));
    }
}
