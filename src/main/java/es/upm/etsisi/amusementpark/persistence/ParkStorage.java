package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.ticket.Ticket;
import es.upm.etsisi.amusementpark.user.User;
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
 * Coordinates the park repositories, all configured from one data directory. Normal and
 * temporary ({@code --fly}) executions use this same class with different directories.
 */
public class ParkStorage {

    private final Path directory;
    private final List<Path> files;
    private final UserFileRepository users;
    private final GroupFileRepository groups;
    private final AttractionFileRepository attractions;
    private final TicketFileRepository tickets;

    public ParkStorage(Path directory, PrintStream warnings) {
        this.directory = directory;
        Path usersFile = directory.resolve("users.txt");
        Path groupsFile = directory.resolve("groups.txt");
        Path attractionsFile = directory.resolve("attractions.txt");
        Path ticketsFile = directory.resolve("tickets.txt");
        this.files = List.of(usersFile, groupsFile, attractionsFile, ticketsFile);
        this.users = new UserFileRepository(usersFile, warnings);
        this.groups = new GroupFileRepository(groupsFile, warnings);
        this.attractions = new AttractionFileRepository(attractionsFile, warnings);
        this.tickets = new TicketFileRepository(ticketsFile, warnings);
    }

    public Path getDirectory() {
        return directory;
    }

    public ParkData load() throws IOException {
        List<User> loadedUsers = users.loadAll();
        Map<String, User> byDni = loadedUsers.stream()
                .collect(Collectors.toMap(User::getDni, Function.identity(), (a, b) -> a));
        List<Group> loadedGroups = groups.load(byDni);
        Map<String, Group> byId = loadedGroups.stream()
                .collect(Collectors.toMap(Group::getId, Function.identity(), (a, b) -> a));
        List<Attraction> loadedAttractions = attractions.load();
        List<Ticket> loadedTickets = tickets.load(byDni, byId);
        return new ParkData(loadedUsers, loadedGroups, loadedTickets, loadedAttractions);
    }

    /**
     * Saves the whole park state. If any file cannot be written, every file is restored to
     * its previous content before the exception is rethrown.
     */
    public void save(List<User> userList, List<Group> groupList, List<Ticket> ticketList,
                     List<Attraction> attractionList) throws IOException {
        Map<Path, byte[]> backup = new LinkedHashMap<>();
        for (Path file : files) {
            backup.put(file, Files.isRegularFile(file) ? Files.readAllBytes(file) : null);
        }
        try {
            users.saveAll(userList);
            groups.save(groupList);
            attractions.save(attractionList);
            tickets.save(ticketList);
        } catch (IOException e) {
            restore(backup);
            throw e;
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
