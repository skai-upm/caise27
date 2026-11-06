package es.upm.park.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.persistence.UserFileStore;
import es.upm.park.repository.UserRepository;
import es.upm.park.service.UserService;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        processor = new CommandProcessor(new UserService(repo));
    }

    @Test
    void createsUserFromCommand() {
        assertEquals("User created: 12345678Z", processor.process(ANA));
        assertEquals("Garcia Lopez", repo.findByDni("12345678Z").orElseThrow().getSurnames());
    }

    @Test
    void commandNameIgnoresSurroundingWhitespace() {
        assertEquals("User created: 12345678Z", processor.process("  " + ANA));
    }

    @Test
    void rejectsWrongFieldCount() {
        assertEquals("Error: USER CREATE expects 7 fields", processor.process("USER CREATE;Ana;Garcia Lopez"));
        assertEquals("Error: USER CREATE expects 7 fields", processor.process(ANA + ";extra"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void reportsUnknownCommand() {
        assertEquals("Error: unknown command 'FOO BAR'", processor.process("FOO BAR;1"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void reportsDomainErrorsAsMessages() {
        processor.process(ANA);
        assertEquals("Error: a user with email ana@correo.com already exists", processor.process(
                "USER CREATE;Luis;Perez;00000000T;ana@correo.com;Clave1!;600000000;1111222233334444"));
        assertEquals("Error: invalid DNI", processor.process(
                "USER CREATE;Luis;Perez;00000000A;luis@correo.com;Clave1!;600000000;1111222233334444"));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void ignoresBlankLines() {
        assertNull(processor.process("   "));
        assertNull(processor.process(null));
    }

    @Test
    void reportsPersistenceFailureAsMessage(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository persistent = new UserRepository(new UserFileStore(file));
        Files.createDirectory(file);
        String result = new CommandProcessor(new UserService(persistent)).process(ANA);
        assertEquals("Error: cannot write " + file, result);
        assertEquals(0, persistent.findAll().size());
    }

    @Test
    void deletesUserAndAllowsRecreation() {
        processor.process(ANA);
        assertEquals("User deleted: ana@correo.com", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, repo.findAll().size());
        assertEquals("User created: 12345678Z", processor.process(ANA));
    }

    @Test
    void rejectsDeleteWithWrongCredentialsOrFields() {
        processor.process(ANA);
        assertEquals("Error: incorrect password", processor.process("USER DELETE;ana@correo.com;Otra2#"));
        assertEquals("Error: no user with email nadie@correo.com",
                processor.process("USER DELETE;nadie@correo.com;Clave1!"));
        assertEquals("Error: USER DELETE expects 2 fields", processor.process("USER DELETE;ana@correo.com"));
        assertEquals(1, repo.findAll().size());
    }
}
