package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.exception.RuleViolationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.persistence.storage.AttractionFileStorage;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeTest {

    private CommandProcessor p;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        p = ApplicationInitializer.initialize(Repositories.inMemory());
        for (String c : List.of("SYSTEM DATE;2027-04-15",
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL")) {
            assertEquals(false, p.process(c).startsWith("ERROR"), c);
        }
    }

    private Attraction<?> a(String id) {
        return Park.getInstance().attractions().findById(id).orElseThrow();
    }

    private Ticket t(String id) {
        return Park.getInstance().tickets().findById(id).orElseThrow();
    }

    @Test
    void typesCreatedAndShown() {
        assertEquals(AttractionType.GENERAL, a("A01").getType());
        assertTrue(p.process("ATTRACTION STATUS;A02").contains("Type: INDIVIDUAL"));
        assertTrue(p.process("ATTRACTION STATUS;A03").contains("Type: GROUP"));
        assertTrue(p.process("ATTRACTION STATUS;A04").contains("Type: SPECIAL"));
        assertTrue(p.process("ATTRACTION CREATE;A05;X;5;VIP").startsWith("ERROR"));
    }

    @Test
    void rejectedCombinations() {
        assertTrue(p.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION ENTER;A03;T001").startsWith("ERROR"));
        for (String id : List.of("A02", "A03", "A04")) {
            assertTrue(a(id).getOccupants().isEmpty(), id);
        }
    }

    @Test
    void acceptedCombinations() {
        assertTrue(p.process("ATTRACTION ENTER;A02;T001").startsWith("ENTRY GRANTED"));
        assertTrue(p.process("ATTRACTION ENTER;A03;TG01").startsWith("ENTRY GRANTED"));
        p.process("ATTRACTION EXIT;A02;ana@correo.com");
        p.process("ATTRACTION EXIT;A03;ana@correo.com");
        p.process("ATTRACTION EXIT;A03;pedro@correo.com");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("ENTRY GRANTED"));
        p.process("ATTRACTION EXIT;A01;ana@correo.com");
        assertTrue(p.process("ATTRACTION ENTER;A01;TG01").startsWith("ENTRY GRANTED"));
    }

    @Test
    void genericBoundRestrictsTypedOperations() {
        IndividualAttraction individual = new IndividualAttraction("X1", "I", 5);
        GroupAttraction group = new GroupAttraction("X2", "G", 5);
        assertFalse(individual.accepts(t("TG01")));
        assertFalse(group.accepts(t("T001")));
        assertThrows(RuleViolationException.class, () -> individual.enter(t("TG01")));
        assertThrows(RuleViolationException.class, () -> group.enter(t("T001")));
        assertTrue(individual.getOccupants().isEmpty() && group.getOccupants().isEmpty());
        assertEquals(2, group.enter(t("TG01")).size());
    }

    @Test
    void legacyLinesLoadAsGeneral() throws IOException {
        Path f = dir.resolve("attractions.txt");
        Files.write(f, List.of("A01;Dragon Rojo;20", "A04;Terror;15;SPECIAL"));
        List<Attraction<?>> loaded = new AttractionFileStorage(f).load();
        assertEquals(AttractionType.GENERAL, loaded.get(0).getType());
        assertEquals(AttractionType.SPECIAL, loaded.get(1).getType());
    }
}
