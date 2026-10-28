package es.upm.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.persistence.InMemoryUserStorage;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;

class CommandProcessorTest {

    static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private final UserRepository repository = new UserRepository();
    private final CommandProcessor processor = new CommandProcessor(new UserService(repository, new InMemoryUserStorage()));

    @Test
    void createsUserFromSemicolonSeparatedCommand() {
        assertEquals("USER CREATED", processor.process(ANA));
        var ana = repository.findByDni("12345678Z").orElseThrow();
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCardNumber());
    }

    @Test
    void unknownCommandIsAnError() {
        assertTrue(processor.process("FOO;bar").startsWith("ERROR: Unknown command"));
    }

    @Test
    void wrongArgumentCountIsAnError() {
        assertTrue(processor.process("USER CREATE;Ana").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rejectedDataIsReportedAndProcessingContinues() {
        assertTrue(processor.process(ANA.replace("12345678Z", "12345678A")).startsWith("ERROR: Invalid DNI"));
        assertEquals("USER CREATED", processor.process(ANA));
        assertTrue(processor.process(ANA).startsWith("ERROR: A user with this DNI"));
        assertTrue(processor.process(ANA.replace("12345678Z", "87654321X")).startsWith("ERROR: A user with this email"));
    }

    @Test
    void blankLinesProduceNoOutput() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }

    @Test
    void userDeleteRemovesUserAndAllowsReuse() {
        processor.process(ANA);
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(repository.findAll().isEmpty());
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void userDeleteErrors() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;nadie@correo.com;Clave1!").startsWith("ERROR: User not found"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Otra1!").startsWith("ERROR: Incorrect password"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR: Wrong number of arguments"));
        assertEquals(1, repository.findAll().size());
    }
}
