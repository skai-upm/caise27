package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserRepositoryTest {

    private static User user(String email) {
        return new User("Ana", "Garcia Lopez", "12345678Z", email,
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void storedUserCanBeRetrievedByEmail() {
        UserRepository repository = new UserRepository();
        User ana = user("ana@correo.com");
        repository.save(ana);
        assertSame(ana, repository.findByEmail("ana@correo.com").orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void unknownEmailReturnsNoUser() {
        assertTrue(new UserRepository().findByEmail("nobody@correo.com").isEmpty());
    }

    @Test
    void deletedUserIsNoLongerRetrievable() {
        UserRepository repository = new UserRepository();
        repository.save(user("ana@correo.com"));
        assertTrue(repository.delete("ana@correo.com"));
        assertTrue(repository.findByEmail("ana@correo.com").isEmpty());
        assertFalse(repository.delete("ana@correo.com"));
    }
}
