package com.parque.persistence;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.business.service.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupRepositoryTest {
    @TempDir
    Path dir;

    final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
            "9876543210987654");
    final Map<String, User> byDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);

    @Test
    void missingFileMeansNoGroups() throws Exception {
        assertTrue(new GroupRepository(dir.resolve("groups.txt")).load(byDni::get).isEmpty());
    }

    @Test
    void roundTripResolvesSameUsers() throws Exception {
        Path file = dir.resolve("groups.txt");
        Group g1 = new Group("G01", "Familia Garcia");
        g1.addMember(ana);
        g1.addMember(pedro);
        Group g2 = new Group("G02", "Vacia");
        GroupRepository repo = new GroupRepository(file);
        repo.save(List.of(g1, g2));
        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacia;"), Files.readAllLines(file));
        List<Group> loaded = repo.load(byDni::get);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertEquals(0, loaded.get(1).getMembers().size());
    }

    @Test
    void unknownDniAndMalformedLinesSkipped() throws Exception {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;Familia;12345678Z,00000000T", "broken", "", "G02;Otra;"));
        List<Group> loaded = new GroupRepository(file).load(byDni::get);
        assertEquals(2, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }
}
