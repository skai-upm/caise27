package es.upm.park.persistence;

import es.upm.park.business.controller.ParkState;
import es.upm.park.business.controller.ParkStorage;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the park state in text files through the repositories it is given.
 * Repositories are created and configured by the application at startup.
 */
public class TextParkStorage implements ParkStorage {

    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;
    private final PassRepository passes;

    public TextParkStorage(GroupRepository groups, TicketRepository tickets, AttractionRepository attractions,
                           PassRepository passes) {
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.passes = passes;
    }

    @Override
    public ParkState load() {
        List<Group> loadedGroups = groups.loadAll();
        Map<String, Group> byId = new LinkedHashMap<>();
        loadedGroups.forEach(group -> byId.put(group.getId(), group));
        List<Ticket> loadedTickets = tickets.loadAll(byId);
        Map<String, IndividualTicket> individuals = new LinkedHashMap<>();
        for (Ticket ticket : loadedTickets) {
            if (ticket instanceof IndividualTicket individual) {
                individuals.put(individual.getId(), individual);
            } else if (ticket instanceof GroupTicket groupTicket) {
                groupTicket.getComposition().forEach(composed -> individuals.put(composed.getId(), composed));
            }
        }
        return new ParkState(loadedGroups, loadedTickets, attractions.loadAll(), passes.loadAll(individuals));
    }

    @Override
    public void save(ParkState state) {
        groups.saveAll(state.groups());
        tickets.saveAll(state.tickets());
        attractions.saveAll(state.attractions());
        passes.saveAll(state.passes());
    }
}
