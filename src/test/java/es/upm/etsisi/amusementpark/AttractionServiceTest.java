package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionServiceTest {

    @TempDir
    Path dir;

    @Test
    void createsAndPersistsDefinitions() throws IOException {
        Path file = dir.resolve("attractions.txt");
        AttractionService service = new AttractionService(new AttractionFileStorage(file));
        Attraction a = service.create("A01", "Tornado", "20");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));
        Files.write(file, List.of("A01;Tornado;20", "", "bad", "A02;X;abc"));
        AttractionService reloaded = new AttractionService(new AttractionFileStorage(file));
        assertEquals(1, reloaded.getAttractions().size());
        assertTrue(reloaded.get("A01").getPresent().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"A01,Otra,10", "A02,'',10", "'',X,10", "A02,X,0", "A02,X,-3", "A02,X,abc", "A02,X,2.5"})
    void rejectsInvalidCreations(String id, String name, String capacity) {
        AttractionService service = new AttractionService(new AttractionFileStorage(dir.resolve("a.txt")));
        service.create("A01", "Tornado", "20");
        assertThrows(IllegalArgumentException.class, () -> service.create(id, name, capacity));
        assertEquals(1, service.getAttractions().size());
    }

    @Test
    void unknownAttraction() {
        AttractionService service = new AttractionService(new AttractionFileStorage(dir.resolve("a.txt")));
        assertThrows(IllegalArgumentException.class, () -> service.get("A99"));
        assertThrows(IllegalArgumentException.class, () -> service.exit("A99", List.of()));
    }

    @Test
    void failedSaveRevertsCreation() throws IOException {
        Path file = dir.resolve("a.txt");
        AttractionService service = new AttractionService(new AttractionFileStorage(file));
        Files.createDirectory(file); // writing to a directory path fails
        assertThrows(RuntimeException.class, () -> service.create("A01", "Tornado", "20"));
        assertTrue(service.getAttractions().isEmpty());
    }
}
