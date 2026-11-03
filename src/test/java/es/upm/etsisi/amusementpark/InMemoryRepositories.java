package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.util.ArrayList;
import java.util.List;

/** Alternative repository implementation keeping data in memory, used to show replaceability. */
final class InMemoryRepositories {

    final List<User> users = new ArrayList<>();
    final List<Group> groups = new ArrayList<>();
    final List<Ticket> tickets = new ArrayList<>();
    final List<Attraction> attractions = new ArrayList<>();
    final List<Pass> passes = new ArrayList<>();

    ParkRepositories bundle() {
        return new ParkRepositories(
                new es.upm.etsisi.amusementpark.business.repository.UserRepository() {
                    @Override
                    public List<User> load() {
                        return new ArrayList<>(users);
                    }

                    @Override
                    public void save(List<User> data) {
                        replace(users, data);
                    }
                },
                new es.upm.etsisi.amusementpark.business.repository.GroupRepository() {
                    @Override
                    public List<Group> load(java.util.Map<String, User> usersByDni) {
                        return new ArrayList<>(groups);
                    }

                    @Override
                    public void save(List<Group> data) {
                        replace(groups, data);
                    }
                },
                new es.upm.etsisi.amusementpark.business.repository.TicketRepository() {
                    @Override
                    public List<Ticket> load(java.util.Map<String, User> usersByDni,
                                             java.util.Map<String, Group> groupsById) {
                        return new ArrayList<>(tickets);
                    }

                    @Override
                    public void save(List<Ticket> data) {
                        replace(tickets, data);
                    }
                },
                new es.upm.etsisi.amusementpark.business.repository.AttractionRepository() {
                    @Override
                    public List<Attraction> load() {
                        return new ArrayList<>(attractions);
                    }

                    @Override
                    public void save(List<Attraction> data) {
                        replace(attractions, data);
                    }
                },
                new es.upm.etsisi.amusementpark.business.repository.PassRepository() {
                    @Override
                    public List<Pass> load(java.util.Map<String, es.upm.etsisi.amusementpark.business.ticket.IndividualTicket> ticketsById) {
                        return new ArrayList<>(passes);
                    }

                    @Override
                    public void save(List<Pass> data) {
                        replace(passes, data);
                    }
                });
    }

    private static <T> void replace(List<T> target, List<T> data) {
        target.clear();
        target.addAll(data);
    }
}
