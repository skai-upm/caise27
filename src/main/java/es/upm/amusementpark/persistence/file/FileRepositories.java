package es.upm.amusementpark.persistence.file;

import java.nio.file.Path;

/**
 * Creates and loads the text-file repositories of one data directory. Normal and --fly executions only differ in
 * the directory passed here.
 */
public final class FileRepositories {

    private final FileUserRepository users;
    private final FileGroupRepository groups;
    private final FileAttractionRepository attractions;
    private final FileTicketRepository tickets;
    private final FilePassRepository passes;

    private FileRepositories(Path directory) {
        users = new FileUserRepository(directory.resolve(FileUserRepository.FILE_NAME));
        groups = new FileGroupRepository(directory.resolve(FileGroupRepository.FILE_NAME), users);
        attractions = new FileAttractionRepository(directory.resolve(FileAttractionRepository.FILE_NAME));
        tickets = new FileTicketRepository(directory.resolve(FileTicketRepository.FILE_NAME), users, groups);
        passes = new FilePassRepository(directory.resolve(FilePassRepository.FILE_NAME), tickets);
    }

    /**
     * Builds the repositories for the directory and loads them in dependency order.
     */
    public static FileRepositories open(Path directory) {
        FileRepositories repositories = new FileRepositories(directory);
        repositories.users.load();
        repositories.groups.load();
        repositories.attractions.load();
        repositories.tickets.load();
        repositories.passes.load();
        return repositories;
    }

    public FileUserRepository users() {
        return users;
    }

    public FileGroupRepository groups() {
        return groups;
    }

    public FileAttractionRepository attractions() {
        return attractions;
    }

    public FileTicketRepository tickets() {
        return tickets;
    }

    public FilePassRepository passes() {
        return passes;
    }
}
