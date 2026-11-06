package es.upm.park.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
