package es.upm.amusementpark.design;

import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationException;
import es.upm.amusementpark.business.exception.RuleViolationException;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Checks the layered organisation and the exception categories.
 */
class ArchitectureTest {

    private static final Path ROOT = Path.of("src/main/java");
    private static final Path APP = ROOT.resolve("es/upm/amusementpark");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void everyClassBelongsToALayer() throws IOException {
        for (Path f : sources(ROOT)) {
            String rel = APP.relativize(f).toString();
            assertTrue(rel.startsWith("presentation/") || rel.startsWith("business/") || rel.startsWith("persistence/"), rel);
            assertTrue(Files.readString(f).startsWith("package es.upm.amusementpark."), rel + " has no package");
        }
    }

    @Test
    void presentationDoesNotTouchDataFiles() throws IOException {
        for (Path f : sources(APP.resolve("presentation"))) {
            String code = Files.readString(f);
            for (String file : List.of("users.txt", "groups.txt", "tickets.txt", "attractions.txt", "Files.write", "readAllLines")) {
                assertFalse(code.contains(file), f + " uses " + file);
            }
        }
    }

    @Test
    void modelIsIndependentOfOtherLayers() throws IOException {
        for (Path f : sources(APP.resolve("business/model"))) {
            String code = Files.readString(f);
            assertFalse(code.contains("import es.upm.amusementpark.persistence"), f.toString());
            assertFalse(code.contains("import es.upm.amusementpark.presentation"), f.toString());
            assertFalse(code.contains("java.nio.file"), f.toString());
        }
    }

    @Test
    void exceptionCategories() {
        Park park = Park.getInstance();
        park.start(Repositories.inMemory(new UserRepository()));
        GroupService groups = GroupService.getInstance().inject(park.users(), park.groups(), park.tickets());
        UserService users = UserService.getInstance().inject(park.users(), park.groups(), park.tickets());
        TicketService tickets = TicketService.getInstance().inject(park.users(), park.tickets(), park.groups());
        assertThrows(NotFoundException.class, () -> groups.findGroup("G99"));
        assertThrows(InvalidDataException.class, () -> users.createUser("A", "B", "12345678A", "a@b.com", "Clave1!", "6", "1234567890123456"));
        users.createUser("A", "B", "12345678Z", "a@b.com", "Clave1!", "6", "1234567890123456");
        groups.createGroup("G01", "F");
        groups.addMember("G01", "a@b.com");
        tickets.createGroupTicket("TG01", "G01", park.getCurrentDate());
        assertThrows(RuleViolationException.class, () -> groups.removeMember("G01", "a@b.com"));
        assertTrue(OperationException.class.isAssignableFrom(RuleViolationException.class));
    }

    @Test
    void rejectedCommandDoesNotStopProcessing() {
        Park.getInstance().start(Repositories.inMemory(new UserRepository()));
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory(Park.getInstance().users()));
        assertTrue(p.process("TICKET DELETE;NOPE").startsWith("ERROR"));
        assertTrue(p.process("HELP").startsWith("Available commands"));
        assertEquals(false, p.isExitRequested());
    }
}
