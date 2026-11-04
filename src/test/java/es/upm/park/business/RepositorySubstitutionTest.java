package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorySubstitutionTest {

    /** Repository implementation kept in memory: proves controllers do not depend on files. */
    private static final class InMemoryUserRepository implements UserRepository {
        private List<User> stored = new ArrayList<>();
        private int saves;

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void save(List<User> users) {
            stored = new ArrayList<>(users);
            saves++;
        }
    }

    @TempDir
    Path dir;

    @Test
    void userManagerWorksWithAnotherRepositoryImplementation() throws IOException {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserManager users = UserManager.configure(repo);
        assertTrue(users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertTrue(users.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999").success());
        assertFalse(users.create("Otra", "X", "12345678Z", "otra@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertEquals(2, repo.saves);
        assertEquals("699999999", repo.stored.get(0).getPhone());
        assertTrue(users.delete("ana@correo.com", "Clave1!").success());
        assertTrue(repo.stored.isEmpty());
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(0, files.count(), "no file written");
        }
    }
}
