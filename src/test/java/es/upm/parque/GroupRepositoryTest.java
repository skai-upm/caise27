package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");

    @Test
    void roundTripKeepsMemberIdentity() throws IOException {
        Path file = dir.resolve("groups.txt");
        GroupRepository repository = new GroupRepository(file);
        Group family = new Group("G01", "Familia Garcia");
        family.addMember(ana);
        family.addMember(pedro);
        repository.saveAll(List.of(family, new Group("G02", "Vacio")));

        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"), Files.readAllLines(file));

        Map<String, User> users = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);
        List<Group> loaded = repository.loadAll(users::get);
        assertEquals(2, loaded.size());
        assertEquals("Familia Garcia", loaded.get(0).getName());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void unknownDnisAreSkipped() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.writeString(file, "G01;Familia;12345678Z,00000000T\nbroken\n");
        List<Group> loaded = new GroupRepository(file).loadAll(Map.of(ana.getDni(), ana)::get);
        assertEquals(1, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }
}
