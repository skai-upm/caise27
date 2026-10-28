package es.upm.amusementpark.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.model.User;

class UserRepositoryTest {

    private final UserRepository repository = new UserRepository();
    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void savedUsersCanBeFoundByDniAndEmail() {
        repository.save(ana);
        assertSame(ana, repository.findByDni("12345678Z").orElseThrow());
        assertSame(ana, repository.findByEmail("ana@correo.com").orElseThrow());
        assertTrue(repository.findByDni("87654321X").isEmpty());
        assertTrue(repository.findByEmail("pedro@correo.com").isEmpty());
    }

    @Test
    void findAllKeepsInsertionOrder() {
        repository.save(pedro);
        repository.save(ana);
        assertEquals(List.of(pedro, ana), repository.findAll());
    }

    @Test
    void deleteRemovesTheUser() {
        repository.save(ana);
        repository.save(pedro);
        repository.delete("12345678Z");
        assertTrue(repository.findByDni("12345678Z").isEmpty());
        assertEquals(List.of(pedro), repository.findAll());
    }
}
