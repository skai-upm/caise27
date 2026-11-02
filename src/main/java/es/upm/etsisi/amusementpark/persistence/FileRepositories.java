package es.upm.etsisi.amusementpark.persistence;

import java.nio.file.Path;

import es.upm.etsisi.amusementpark.business.repository.Repositories;

/**
 * Builds the text-file repositories for one data directory. Normal and --fly mode
 * use the same classes and differ only in the directory passed here.
 */
public final class FileRepositories {
    private FileRepositories() {
    }

    public static Repositories in(Path directory) {
        return new Repositories(new FileUserRepository(directory.resolve("users.txt")),
                new FileGroupRepository(directory.resolve("groups.txt")),
                new FileTicketRepository(directory.resolve("tickets.txt")),
                new FileAttractionRepository(directory.resolve("attractions.txt")),
                new FilePassRepository(directory.resolve("passes.txt")));
    }
}
