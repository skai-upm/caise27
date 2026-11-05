package es.upm.amusementpark.business.service;

import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;

import es.upm.amusementpark.business.exception.OperationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.persistence.storage.UserFileStorage;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserDeletionTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;
    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository(new UserFileStorage(dir.resolve("users.txt")));
        processor = ApplicationInitializer.initialize(Repositories.inMemory(repo));
        processor.process(ANA);
        processor.process(PEDRO);
    }

    @Test
    void matchingCredentialsDeleteAndPersist() {
        assertEquals("USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#"));
        assertTrue(repo.findByEmail("pedro@correo.com").isEmpty());
        UserRepository restarted = new UserRepository(new UserFileStorage(dir.resolve("users.txt")));
        assertEquals(1, restarted.count());
        assertEquals("12345678Z", restarted.findAll().get(0).getDni());
    }

    @Test
    void wrongPasswordKeepsUser() {
        assertTrue(processor.process("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR"));
        assertTrue(repo.findByEmail("ana@correo.com").isPresent());
        assertEquals(2, new UserRepository(new UserFileStorage(dir.resolve("users.txt"))).count());
    }

    @Test
    void unknownEmailAndWrongArgumentCountRejected() {
        assertTrue(processor.process("USER DELETE;nadie@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertEquals(2, repo.count());
    }

    @Test
    void deletedUserCanBeCreatedAgain() {
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void serviceRejectsWrongPassword() {
        UserService service = UserService.getInstance().inject(repo, new GroupRepository(), new TicketRepository());
        assertThrows(OperationException.class, () -> service.deleteUser("ana@correo.com", "nope"));
    }
}
