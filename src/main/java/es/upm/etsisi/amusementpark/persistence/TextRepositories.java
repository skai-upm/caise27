package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Creates the text-file repositories for one data directory. Normal and
 * temporary (--fly) executions differ only in that directory.
 */
public final class TextRepositories {

    private TextRepositories() {
    }

    public static ParkRepositories inDirectory(Path directory, PrintStream warnings) {
        return new ParkRepositories(
                new TextUserRepository(directory.resolve("users.txt"), warnings),
                new TextGroupRepository(directory.resolve("groups.txt"), warnings),
                new TextTicketRepository(directory.resolve("tickets.txt"), warnings),
                new TextAttractionRepository(directory.resolve("attractions.txt"), warnings),
                new TextPassRepository(directory.resolve("passes.txt"), warnings));
    }
}
