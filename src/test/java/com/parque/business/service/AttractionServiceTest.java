package com.parque.business.service;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionServiceTest {
    @TempDir
    Path dir;

    @Test
    void createPersistsDefinitionsOnly() throws Exception {
        AttractionService s = new AttractionService(new AttractionRepository(dir.resolve("attractions.txt")));
        Attraction a = s.create("A01", "Tornado", "20");
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        LocalDate today = LocalDate.of(2026, 10, 15);
        a.enter(new IndividualTicket("T001", ana, today, new BigDecimal("40")), today);
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));

        AttractionService restarted = new AttractionService(new AttractionRepository(dir.resolve("attractions.txt")));
        Attraction again = restarted.find("A01");
        assertEquals("Tornado", again.getName());
        assertEquals(20, again.getCapacity());
        assertEquals(0, again.getPresent().size(), "attractions start empty");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"A01|Otra|10", "A02|Otra|0", "A02|Otra|-3", "A02|Otra|diez", "A02|' '|5",
            "' '|Otra|5", "A02|Otra|''", "A02|Otra|99999999999"})
    void invalidCreations(String id, String name, String capacity) throws Exception {
        AttractionService s = new AttractionService(new AttractionRepository(dir.resolve("attractions.txt")));
        s.create("A01", "Tornado", "20");
        assertThrows(IllegalArgumentException.class, () -> s.create(id, name, capacity));
        assertEquals(1, s.getAttractions().size());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));
    }

    @Test
    void unknownAttraction() throws Exception {
        AttractionService s = new AttractionService(new AttractionRepository(dir.resolve("attractions.txt")));
        assertThrows(IllegalArgumentException.class, () -> s.existing("A99"));
    }
}
