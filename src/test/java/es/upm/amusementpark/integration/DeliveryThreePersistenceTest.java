package es.upm.amusementpark.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryThreePersistenceTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String FUTURE = LocalDate.now().plusDays(10).toString();

    @TempDir
    Path dir;

    private CommandProcessor start() {
        return ApplicationInitializer.initialize(Repositories.files(dir));
    }

    private Pass pass(String id) {
        return Park.getInstance().passes().findById(id).orElseThrow();
    }

    private void setUp() {
        CommandProcessor p = start();
        for (String c : List.of(
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;" + TODAY, "TICKET CREATE;T009;ana@correo.com;" + FUTURE,
                "GROUPTICKET CREATE;TG01;G01;" + FUTURE,
                "SPEEDPASS CREATE;SP02;T001;10", "SPECIALPASS CREATE;PE01;T001", "SPEEDPASS CREATE;SP03;TG01:12345678Z;1",
                "SPEEDPASS CREATE;SP09;T009;10", "SPECIALPASS CREATE;PE09;T009",
                "ATTRACTION CREATE;A01;D;20", "ATTRACTION CREATE;A02;I;5;INDIVIDUAL",
                "ATTRACTION CREATE;A03;G;5;GROUP", "ATTRACTION CREATE;A04;S;5;SPECIAL",
                "ATTRACTION ENTER;A01;T001;SPEED")) {
            assertFalse(p.process(c).startsWith("ERROR"), c);
        }
    }

    @Test
    void passesAndTypesReconstructed() {
        setUp();
        CommandProcessor p = start();
        assertTrue(pass("SP02:1").isConsumed());
        for (int i = 2; i <= 10; i++) {
            assertFalse(pass("SP02:" + i).isConsumed());
            assertEquals(new BigDecimal("8.00"), pass("SP02:" + i).getPrice());
        }
        assertEquals("T001", pass("PE01").getTicket().getIdentifier());
        assertEquals("TG01:12345678Z", pass("SP03:1").getTicket().getIdentifier());
        assertTrue(p.process("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        assertEquals(AttractionType.SPECIAL, Park.getInstance().attractions().findById("A04").orElseThrow().getType());
        assertEquals(AttractionType.GROUP, Park.getInstance().attractions().findById("A03").orElseThrow().getType());
        assertTrue(Park.getInstance().attractions().findById("A01").orElseThrow().getOccupants().isEmpty());
    }

    @Test
    void refundsAndCascadesReflected() {
        setUp();
        CommandProcessor p = start();
        assertEquals("SPEEDPASS DELETED", p.process("SPEEDPASS DELETE;SP09:2"));
        assertEquals("TICKET DELETED", p.process("TICKET DELETE;T009"));
        assertEquals("GROUP TICKET DELETED", p.process("GROUPTICKET DELETE;TG01"));
        start();
        assertTrue(Park.getInstance().passes().findAll().stream().noneMatch(x -> x.getIdentifier().startsWith("SP09")
                || x.getIdentifier().equals("PE09") || x.getIdentifier().equals("SP03:1")));
        assertTrue(Park.getInstance().passes().findById("SP02:1").isPresent());
    }

    @Test
    void flyModeCoversPasses() throws Exception {
        Path input = dir.resolve("in.txt");
        Files.write(input, List.of("USER CREATE;Ana;G;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;" + FUTURE, "SPEEDPASS CREATE;SP01;T001;1"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeliveryOneIntegrationTest.runApp(new String[] {"--fly", input.toString()}, "", out, new ByteArrayOutputStream(),
                dir.resolve("users.txt"));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("SPEEDPASS CREATED"));
        assertFalse(Files.exists(dir.resolve("passes.txt")));
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
