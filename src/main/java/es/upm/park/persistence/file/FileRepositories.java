package es.upm.park.persistence.file;

import es.upm.park.persistence.DataPaths;
import es.upm.park.persistence.Repositories;

/**
 * Builds the text-file repositories for a data directory. Normal and
 * temporary (--fly) execution only pass different paths.
 */
public final class FileRepositories {

    private FileRepositories() {
    }

    public static Repositories create(DataPaths paths) {
        return new Repositories(
                new UserFileRepository(paths.users()),
                new GroupFileRepository(paths.groups()),
                new TicketFileRepository(paths.tickets()),
                new AttractionFileRepository(paths.attractions()),
                new PassFileRepository(paths.passes()));
    }
}
