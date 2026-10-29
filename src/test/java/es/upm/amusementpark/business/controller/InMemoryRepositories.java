package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.ticket.Ticket;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.repository.GroupRepository;
import es.upm.amusementpark.business.repository.PassRepository;
import es.upm.amusementpark.business.repository.Repositories;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.business.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Alternative repository implementations that keep data in memory only.
 */
final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    static Repositories create() {
        return new Repositories(new Users(), new Groups(), new Tickets(), new Attractions(), new Passes());
    }

    static final class Users implements UserRepository {
        private List<User> stored = new ArrayList<>();

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<User> users) {
            stored = new ArrayList<>(users);
        }
    }

    static final class Groups implements GroupRepository {
        private List<Group> stored = new ArrayList<>();

        @Override
        public List<Group> load(Map<String, User> usersByDni) {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<Group> groups) {
            stored = new ArrayList<>(groups);
        }
    }

    static final class Tickets implements TicketRepository {
        private List<Ticket> stored = new ArrayList<>();

        @Override
        public List<Ticket> load(Map<String, User> activeUsersByDni, Map<String, Group> groupsById) {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<Ticket> tickets, Predicate<User> isActiveUser) {
            stored = new ArrayList<>(tickets);
        }
    }

    static final class Attractions implements AttractionRepository {
        private List<Attraction<?>> stored = new ArrayList<>();

        @Override
        public List<Attraction<?>> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<Attraction<?>> attractions) {
            stored = new ArrayList<>(attractions);
        }
    }

    static final class Passes implements PassRepository {
        private List<Pass> stored = new ArrayList<>();

        @Override
        public List<Pass> load(Map<String, Ticket> ticketsById) {
            return new ArrayList<>(stored);
        }

        @Override
        public void saveAll(Collection<Pass> passes) {
            stored = new ArrayList<>(passes);
        }
    }
}
