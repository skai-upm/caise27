package es.upm.etsisi.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.upm.etsisi.amusementpark.persistence.UserRepository;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserService;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandRunnerTest {

    private static class Memory implements UserRepository {
        private List<User> saved = new ArrayList<>();

        @Override
        public List<User> loadAll() {
            return saved;
        }

        @Override
        public void saveAll(List<User> users) {
            saved = new ArrayList<>(users);
        }
    }

    @Test
    void stopsAtExitAndIgnoresBlankLines() throws Exception {
        UserService service = new UserService(new Memory());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CommandRunner runner = new CommandRunner(new CommandProcessor(service),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        runner.run(new BufferedReader(new StringReader(String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "   ",
                "",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"))));
        assertEquals(1, service.getUsers().size());
        assertEquals(List.of("USER CREATED", "BYE"), bytes.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void endOfInputEndsProcessing() throws Exception {
        UserService service = new UserService(new Memory());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new CommandRunner(new CommandProcessor(service), new PrintStream(bytes, true, StandardCharsets.UTF_8))
                .run(new BufferedReader(new StringReader("HELP\n")));
        assertEquals(7, bytes.toString(StandardCharsets.UTF_8).lines().count());
    }
}
