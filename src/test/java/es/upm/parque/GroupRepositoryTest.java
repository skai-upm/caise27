package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void missingFileMeansNoGroups() {
        assertTrue(new GroupRepository(dir.resolve("none.txt")).loadAll(Map.of()).isEmpty());
    }

    @Test
    void roundTripLinksExistingUsers() throws IOException {
        Path file = dir.resolve("data").resolve("groups.txt");
        GroupRepository repo = new GroupRepository(file);
        Group family = new Group("G01", "Familia Garcia");
        family.addMember(ana);
        family.addMember(pedro);
        repo.saveAll(List.of(family, new Group("G02", "Vacio")));

        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"), Files.readAllLines(file));

        List<Group> loaded = repo.loadAll(Map.of("12345678Z", ana, "87654321X", pedro));
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void unknownDnisDuplicatesAndMalformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;F;12345678Z,00000000T,12345678Z", "broken"));
        List<Group> loaded = new GroupRepository(file).loadAll(Map.of("12345678Z", ana));
        assertEquals(1, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }

    @Test
    void ioErrorsAreUnchecked() {
        GroupRepository repo = new GroupRepository(dir);
        Map<String, User> none = Map.of();
        List<Group> empty = List.of();
        assertThrows(UncheckedIOException.class, () -> repo.loadAll(none));
        assertThrows(UncheckedIOException.class, () -> repo.saveAll(empty));
    }
}
