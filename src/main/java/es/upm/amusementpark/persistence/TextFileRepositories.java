package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.repository.Repositories;

import java.nio.file.Path;

/**
 * Creates the text-file repositories for one data directory. Normal and
 * temporary executions use this same factory with different directories.
 */
public final class TextFileRepositories {

    private TextFileRepositories() {
    }

    public static Repositories create(Path directory) {
        return new Repositories(
                new TextFileUserRepository(directory.resolve("users.txt")),
                new TextFileGroupRepository(directory.resolve("groups.txt")),
                new TextFileTicketRepository(directory.resolve("tickets.txt")),
                new TextFileAttractionRepository(directory.resolve("attractions.txt")),
                new TextFilePassRepository(directory.resolve("passes.txt")));
    }
}
