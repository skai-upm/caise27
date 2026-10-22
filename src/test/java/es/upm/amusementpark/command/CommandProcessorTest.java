package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.InMemoryUserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    private InMemoryUserRepository repository;
    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        repository = new InMemoryUserRepository();
        service = new UserService(repository);
        processor = new CommandProcessor(service);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createsUser() {
        CommandResult result = processor.process(CREATE_ANA);
        assertEquals("USER CREATED", result.output());
        assertFalse(result.exit());
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void keywordIsCaseInsensitiveAndFieldsAreTrimmed() {
        assertEquals("USER CREATED",
                run(" user create ; Ana ;Garcia;12345678Z; ana@correo.com ;Clave1!;612345678;1234567890123456 "));
        assertEquals("Ana", service.findByEmail("ana@correo.com").getFirstName());
    }

    @Test
    void reportsInvalidDataWithoutCreating() {
        String output = run("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains("DNI"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void reportsDuplicates() {
        run(CREATE_ANA);
        assertTrue(run(CREATE_ANA).startsWith("ERROR"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void reportsWrongNumberOfArguments() {
        String output = run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678");
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains(CommandProcessor.USER_CREATE_SYNTAX));
        assertTrue(run(CREATE_ANA + ";extra").startsWith("ERROR"));
        assertTrue(run(CREATE_ANA + ";").startsWith("ERROR"));
    }

    @Test
    void reportsUnknownCommand() {
        CommandResult result = processor.process("FOO;bar");
        assertTrue(result.output().startsWith("ERROR"));
        assertFalse(result.exit());
        assertTrue(run("USER").startsWith("ERROR"));
        assertTrue(run(";;;").startsWith("ERROR"));
    }

    @Test
    void helpListsAllCommands() {
        CommandResult result = processor.process("HELP");
        assertFalse(result.exit());
        String output = result.output();
        assertTrue(output.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(output.contains("USER DELETE;<email>;<password>"));
        assertTrue(output.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(output.contains("HELP"));
        assertTrue(output.contains("EXIT"));
        assertEquals(output, run("help"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
        assertTrue(processor.process(" exit ").exit());
        CommandResult withArgs = processor.process("EXIT;now");
        assertFalse(withArgs.exit());
        assertTrue(withArgs.output().startsWith("ERROR"));
        assertTrue(run("HELP;x").startsWith("ERROR"));
    }

    @Test
    void blankLineProducesNoOutput() {
        assertEquals("", run("   "));
        assertEquals("", run(""));
    }

    @Test
    void persistenceFailureIsReported() {
        repository.setFailOnSave(true);
        String output = run(CREATE_ANA);
        assertTrue(output.startsWith("ERROR"));
        assertTrue(service.getUsers().isEmpty());
    }
}
