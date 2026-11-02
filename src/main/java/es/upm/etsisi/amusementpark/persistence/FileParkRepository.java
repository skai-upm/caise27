package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.park.ParkData;
import es.upm.etsisi.amusementpark.business.park.ParkRepository;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Text-file {@link ParkRepository}: one file per entity inside a configurable directory.
 * Normal and temporary ({@code --fly}) executions use this same class with different directories.
 */
public class FileParkRepository implements ParkRepository {

    private final Path directory;
    private final List<Path> files;
    private final UserFileRepository users;
    private final GroupFileRepository groups;
    private final AttractionFileRepository attractions;
    private final TicketFileRepository tickets;
    private final PassFileRepository passes;

    public FileParkRepository(Path directory, PrintStream warnings) {
        this.directory = directory;
        Path usersFile = directory.resolve("users.txt");
        Path groupsFile = directory.resolve("groups.txt");
        Path attractionsFile = directory.resolve("attractions.txt");
        Path ticketsFile = directory.resolve("tickets.txt");
        Path passesFile = directory.resolve("passes.txt");
        this.files = List.of(usersFile, groupsFile, attractionsFile, ticketsFile, passesFile);
        this.users = new UserFileRepository(usersFile, warnings);
        this.groups = new GroupFileRepository(groupsFile, warnings);
        this.attractions = new AttractionFileRepository(attractionsFile, warnings);
        this.tickets = new TicketFileRepository(ticketsFile, warnings);
        this.passes = new PassFileRepository(passesFile, warnings);
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public ParkData load() throws StorageException {
        try {
            List<User> loadedUsers = users.loadAll();
            Map<String, User> byDni = loadedUsers.stream()
                    .collect(Collectors.toMap(User::getDni, Function.identity(), (a, b) -> a));
            List<Group> loadedGroups = groups.load(byDni);
            Map<String, Group> byId = loadedGroups.stream()
                    .collect(Collectors.toMap(Group::getId, Function.identity(), (a, b) -> a));
            List<Attraction<?>> loadedAttractions = attractions.load();
            List<Ticket> loadedTickets = tickets.load(byDni, byId);
            Map<String, IndividualTicket> individuals = loadedTickets.stream()
                    .flatMap(t -> t.getIndividualTickets().stream())
                    .collect(Collectors.toMap(Ticket::getId, Function.identity(), (a, b) -> a));
            List<Pass> loadedPasses = passes.load(individuals);
            return new ParkData(loadedUsers, loadedGroups, loadedTickets, loadedPasses, loadedAttractions);
        } catch (IOException | RuntimeException e) {
            throw new StorageException("could not load park data from " + directory.toAbsolutePath()
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Saves the whole park state. If any file cannot be written, every file is restored to its
     * previous content before the failure is reported.
     */
    @Override
    public void save(ParkData data) throws StorageException {
        Map<Path, byte[]> backup = new LinkedHashMap<>();
        try {
            for (Path file : files) {
                backup.put(file, Files.isRegularFile(file) ? Files.readAllBytes(file) : null);
            }
            users.saveAll(data.users());
            groups.save(data.groups());
            attractions.save(data.attractions());
            tickets.save(data.tickets());
            passes.save(data.passes());
        } catch (IOException e) {
            restore(backup);
            throw new StorageException("could not save park data: " + e.getMessage(), e);
        }
    }

    private static void restore(Map<Path, byte[]> backup) {
        for (Map.Entry<Path, byte[]> entry : backup.entrySet()) {
            try {
                if (entry.getValue() == null) {
                    Files.deleteIfExists(entry.getKey());
                } else {
                    Files.write(entry.getKey(), entry.getValue());
                }
            } catch (IOException ignored) {
                // Best effort: the original failure is reported to the caller.
            }
        }
    }
}
