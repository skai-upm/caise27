package es.upm.etsisi.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.persistence.UserRepository;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserService service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        UserRepository memory = new UserRepository() {
            private List<User> saved = new ArrayList<>();

            @Override
            public List<User> loadAll() {
                return saved;
            }

            @Override
            public void saveAll(List<User> users) {
                saved = new ArrayList<>(users);
            }
        };
        service = new UserService(memory);
        processor = new CommandProcessor(service);
    }

    @Test
    void createDeleteUpdate() {
        assertEquals("USER CREATED", processor.process(ANA).output());
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output());
        assertEquals("Ana Maria", service.findByEmail("ana@correo.com").orElseThrow().getFirstName());
        assertEquals("USER DELETED", processor.process("  USER DELETE;ana@correo.com;Clave1!  ").output());
        assertTrue(service.getUsers().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER CREATE;Ana;Garcia Lopez",
        "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;ana@correo.com",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "FOO BAR",
        "HELP;extra",
        "EXIT;now"
    })
    void rejectedCommandsReportErrorAndDoNotExit(String line) {
        processor.process(ANA);
        CommandResult result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
        assertFalse(result.exit());
        assertEquals(1, service.getUsers().size());
        assertEquals("Ana", service.getUsers().get(0).getFirstName());
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP").output();
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
    }
}
