package es.upm.park.persistence;

import es.upm.park.business.controller.ParkState;
import es.upm.park.business.controller.ParkStorage;
import es.upm.park.business.model.Group;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Keeps the park state in text files under the configured directory. */
public class TextParkStorage implements ParkStorage {

    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public TextParkStorage(StoragePaths paths, PrintStream warnings) {
        this.groups = new GroupRepository(paths.groups(), warnings);
        this.tickets = new TicketRepository(paths.tickets(), warnings);
        this.attractions = new AttractionRepository(paths.attractions(), warnings);
    }

    @Override
    public ParkState load() {
        List<Group> loadedGroups = groups.loadAll();
        Map<String, Group> byId = new LinkedHashMap<>();
        loadedGroups.forEach(group -> byId.put(group.getId(), group));
        return new ParkState(loadedGroups, tickets.loadAll(byId), attractions.loadAll());
    }

    @Override
    public void save(ParkState state) {
        groups.saveAll(state.groups());
        tickets.saveAll(state.tickets());
        attractions.saveAll(state.attractions());
    }
}
