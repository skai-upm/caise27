package es.upm.etsisi.amusementpark.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class UserServiceTest {

    private InMemoryRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() throws Exception {
        repo = new InMemoryRepository();
        service = new UserService(repo);
        service.create(UserTest.ana());
        service.create(new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
                "Otra2#", "623456789", "9876543210987654"));
    }

    private void assertUnchanged(Runnable r) {
        int saves = repo.saves;
        r.run();
        assertEquals(saves, repo.saves);
        assertEquals(2, service.getUsers().size());
        User ana = service.findByEmail("ana@correo.com").orElseThrow();
        assertEquals("Ana", ana.getFirstName());
        assertEquals("1234567890123456", ana.getCard());
        assertTrue(ana.hasPassword("Clave1!"));
    }

    private void rejects(Executable action) {
        assertUnchanged(() -> assertThrows(UserException.class, action));
    }

    @Test
    void createPersists() {
        assertEquals(2, service.getUsers().size());
        assertEquals(2, repo.saved.size());
    }

    @Test
    void createRejectsInvalidData() {
        rejects(() -> service.create(new User("A", "B", "12345678A", "x@y.com", "Clave1!", "1", "1234567890123456")));
        rejects(() -> service.create(new User("A", "B", "11111111H", "bad", "Clave1!", "1", "1234567890123456")));
        rejects(() -> service.create(new User("A", "B", "11111111H", "x@y.com", "clave1!", "1", "1234567890123456")));
        rejects(() -> service.create(new User("A", "B", "11111111H", "x@y.com", "Clave1!", "1", "1234")));
    }

    @Test
    void createRejectsDuplicates() {
        rejects(() -> service.create(new User("A", "B", "12345678Z", "new@correo.com", "Clave1!", "1", "1234567890123456")));
        rejects(() -> service.create(new User("A", "B", "11111111H", "ana@correo.com", "Clave1!", "1", "1234567890123456")));
    }

    @Test
    void deleteRequiresCredentials() throws Exception {
        rejects(() -> service.delete("nobody@correo.com", "Clave1!"));
        rejects(() -> service.delete("ana@correo.com", "Wrong1!"));
        service.delete("pedro@correo.com", "Otra2#");
        assertEquals(1, service.getUsers().size());
        assertEquals(1, repo.saved.size());
        assertEquals("12345678Z", repo.saved.get(0).getDni());
    }

    @Test
    void updateEachField() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", UserField.EMAIL, "ana.nueva@correo.com");
        User saved = repo.saved.get(0);
        assertEquals("Ana Maria", saved.getFirstName());
        assertEquals("Garcia", saved.getSurnames());
        assertEquals("699999999", saved.getPhone());
        assertEquals("9999888877776666", saved.getCard());
        assertEquals("Nueva2#", saved.getPassword());
        assertEquals("ana.nueva@correo.com", saved.getEmail());
        assertEquals("12345678Z", saved.getDni());
    }

    @Test
    void updateKeepsOwnEmail() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals("ana@correo.com", repo.saved.get(0).getEmail());
    }

    @Test
    void updateRejections() {
        rejects(() -> service.update("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        rejects(() -> service.update("nobody@correo.com", "Clave1!", UserField.NAME, "X"));
        rejects(() -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        rejects(() -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        rejects(() -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        rejects(() -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
    }

    @Test
    void saveFailureLeavesMemoryUnchanged() {
        repo.failOnSave = true;
        rejects(() -> service.update("ana@correo.com", "Clave1!", UserField.NAME, "X"));
        rejects(() -> service.delete("ana@correo.com", "Clave1!"));
        rejects(() -> service.create(new User("A", "B", "11111111H", "x@y.com", "Clave1!", "1", "1234567890123456")));
    }

    @Test
    void loadsExistingUsers() throws IOException {
        UserService reloaded = new UserService(repo);
        assertEquals(2, reloaded.getUsers().size());
        assertThrows(UnsupportedOperationException.class, () -> reloaded.getUsers().clear());
    }
}
