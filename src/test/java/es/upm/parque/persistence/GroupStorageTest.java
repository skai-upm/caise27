package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupStorageTest {

    @TempDir
    Path dir;

    private final User ana =
        new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    private final User pedro =
        new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");

    @Test
    void missingFileLoadsNoGroups() {
        assertTrue(new GroupStorage(dir.resolve("groups.txt")).load(dni -> null).isEmpty());
    }

    @Test
    void roundTripLinksMembersToGivenUsers() throws IOException {
        Path file = dir.resolve("groups.txt");
        Group family = new Group("G01", "Familia Garcia");
        family.addMember(ana);
        family.addMember(pedro);
        GroupStorage storage = new GroupStorage(file);
        storage.save(List.of(family, new Group("G02", "Vacio")));

        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"), Files.readAllLines(file));

        Map<String, User> byDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);
        List<Group> loaded = storage.load(byDni::get);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void unknownDnisAndMalformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("bad line", "G01;Familia;12345678Z,00000000T"));
        List<Group> loaded = new GroupStorage(file).load(dni -> dni.equals(ana.getDni()) ? ana : null);
        assertEquals(1, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }
}
