package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;

import java.nio.file.Path;

/**
 * Creates text-file repositories stored in one directory. Normal and temporary
 * ({@code --fly}) persistence use the same classes with different directories.
 */
public final class TextRepositories {

    private TextRepositories() {
    }

    public static ParkRepositories inDirectory(Path directory) {
        return new ParkRepositories(
                new TextUserRepository(directory.resolve("users.txt")),
                new TextGroupRepository(directory.resolve("groups.txt")),
                new TextTicketRepository(directory.resolve("tickets.txt")),
                new TextAttractionRepository(directory.resolve("attractions.txt")));
    }
}
