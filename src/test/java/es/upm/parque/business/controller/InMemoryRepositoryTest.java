package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.Fixtures;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.UserRepository;

/**
 * Controllers depend on repository abstractions: an alternative implementation works unchanged.
 */
class InMemoryRepositoryTest {

    static final class InMemoryUserRepository implements UserRepository {
        private final List<User> stored = new ArrayList<>();
        private int saves;

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void save(Collection<User> users) {
            stored.clear();
            stored.addAll(users);
            saves++;
        }
    }

    @Test
    void userManagerWorksWithInMemoryStorage() throws IOException {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserManager users = UserManager.initialize(repository);
        assertEquals(UserManager.USER_CREATED, users.create(Fixtures.ana()));
        assertEquals(UserManager.USER_UPDATED, users.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals(2, repository.saves);
        assertEquals("699999999", repository.load().get(0).getPhone());
        assertEquals(1, UserManager.initialize(repository).count());
    }
}
