package es.upm.park.business;

import es.upm.park.business.model.Attraction;
import es.upm.park.persistence.AttractionFileStore;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionManagerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private Path file;
    private AttractionManager manager;
    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
            "Clave1!", "612345678", "1234567890123456");
    private final Ticket t001 = new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana);

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("attractions.txt");
        manager = AttractionManager.configure(new AttractionFileStore(file));
    }

    @Test
    void createPersistsDefinition() throws IOException {
        assertTrue(manager.create("A01", "Tornado", "20").success());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));
    }

    @ParameterizedTest
    @CsvSource({"A02,Loop,0", "A02,Loop,-3", "A02,Loop,diez", "A02,Loop,''", "'',Loop,5", "A02,'',5",
        "A02,Loop,99999999999", "A01,Otra,10"})
    void invalidCreationsRejected(String id, String name, String capacity) throws IOException {
        manager.create("A01", "Tornado", "20");
        assertFalse(manager.create(id, name, capacity).success());
        assertEquals(1, manager.getAttractions().size());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));
    }

    @Test
    void restartRestoresEmptyAttractions() throws IOException {
        manager.create("A01", "Tornado", "20");
        manager.enter("A01", Optional.of(t001), "T001", DAY);
        AttractionManager again = AttractionManager.configure(new AttractionFileStore(file));
        Attraction a = again.find("A01").orElseThrow();
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void statusShowsAllRequiredInformation() {
        manager.create("A01", "Tornado", "20");
        manager.enter("A01", Optional.of(t001), "T001", DAY);
        String status = manager.status("A01").toString();
        assertTrue(status.startsWith("OK: Attraction A01 - Tornado"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Present: 1"));
        assertTrue(status.contains("Available: 19"));
        assertTrue(status.contains("Ana Garcia Lopez (ana@correo.com)"));
        assertFalse(manager.status("A99").success());
    }

    @Test
    void unknownAttractionOrTicket() {
        manager.create("A01", "Tornado", "20");
        assertFalse(manager.enter("A99", Optional.of(t001), "T001", DAY).success());
        assertFalse(manager.enter("A01", Optional.empty(), "T999", DAY).success());
        assertFalse(manager.exit("A99", Optional.of(t001), "T001").success());
        assertFalse(manager.exit("A01", Optional.empty(), "T999").success());
    }

    @Test
    void removeUserEverywhere() {
        manager.create("A01", "Tornado", "20");
        manager.create("A02", "Loop", "5");
        manager.enter("A01", Optional.of(t001), "T001", DAY);
        manager.enter("A02", Optional.of(t001), "T001", DAY);
        manager.removeUserEverywhere(ana);
        assertEquals(20, manager.find("A01").orElseThrow().getAvailable());
        assertEquals(5, manager.find("A02").orElseThrow().getAvailable());
    }

    @Test
    void malformedFileLinesSkipped() throws IOException {
        Files.write(file, List.of("A01;Tornado;20", "A02;Loop;0", "bad", "", "A03;Rio;x"));
        assertEquals(1, AttractionManager.configure(new AttractionFileStore(file)).getAttractions().size());
    }
}
