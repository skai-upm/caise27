package es.upm.etsisi.park.persistence.text;

import java.io.PrintStream;
import java.nio.file.Path;

import es.upm.etsisi.park.persistence.Repositories;

/**
 * Creates the text-file repositories for one data directory. Normal and
 * temporary (--fly) runs only differ in the directory passed here.
 */
public final class TextRepositories {

    public static final String DATA_DIR_PROPERTY = "park.data.dir";
    public static final String DEFAULT_DATA_DIR = ".";

    private TextRepositories() {
    }

    public static Repositories in(Path directory, PrintStream warnings) {
        return new Repositories(
                new TextUserRepository(directory.resolve(TextUserRepository.FILE_NAME), warnings),
                new TextGroupRepository(directory.resolve(TextGroupRepository.FILE_NAME), warnings),
                new TextTicketRepository(directory.resolve(TextTicketRepository.FILE_NAME), warnings),
                new TextPassRepository(directory.resolve(TextPassRepository.FILE_NAME), warnings),
                new TextAttractionRepository(directory.resolve(TextAttractionRepository.FILE_NAME), warnings));
    }

    /** The data directory configured with {@value #DATA_DIR_PROPERTY}, or the working directory. */
    public static Path defaultDirectory() {
        return Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR));
    }
}
