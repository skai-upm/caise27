package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.persistence.DataPaths;
import es.upm.etsisi.park.persistence.Repositories;
import es.upm.etsisi.park.persistence.UserRepository;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.AttractionRepository;
import es.upm.etsisi.park.persistence.PassRepository;

/**
 * Creates a fresh set of file-backed repositories for a data directory. Each call returns
 * new instances; normal and --fly executions only differ in the {@link DataPaths}.
 */
public final class FileRepositories {

    private FileRepositories() {
    }

    public static Repositories open(DataPaths paths) {
        UserRepository users = new FileUserRepository(new UserFileStore(paths.users()));
        GroupRepository groups = new FileGroupRepository(new GroupFileStore(paths.groups()), users);
        AttractionRepository attractions = new FileAttractionRepository(new AttractionFileStore(paths.attractions()));
        TicketRepository tickets = new FileTicketRepository(new TicketFileStore(paths.tickets()), users, groups);
        PassRepository passes = new FilePassRepository(new PassFileStore(paths.passes()), tickets);
        return new Repositories(users, groups, tickets, attractions, passes);
    }
}
