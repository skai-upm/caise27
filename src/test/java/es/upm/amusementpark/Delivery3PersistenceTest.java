package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.DataDirectories;
import es.upm.amusementpark.persistence.TextFileRepositories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Delivery 3 elements and states persisted through the application in both modes. */
class Delivery3PersistenceTest {

    private static final String SCRIPT = "SYSTEM DATE;2027-04-15\n"
            + "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL\n"
            + "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP\n"
            + "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL\n"
            + "SPEEDPASS CREATE;SP01;T001;10\n"
            + "SPECIALPASS CREATE;PE01;T001\n"
            + "SPEEDPASS CREATE;SPA;TG01:12345678Z;1\n"
            + "SPEEDPASS CREATE;SPB;TG01:87654321X;1\n"
            + "SPEEDPASS CREATE;SPF;T009;1\n"
            + "ATTRACTION ENTER;A01;T001;SPEED\n"
            + "ATTRACTION ENTER;A04;T001\n"
            + "ATTRACTION ENTER;A03;TG01;SPEED\n"
            + "TICKET DELETE;T009\n"
            + "EXIT\n";

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path base;

    /** Users can only be registered programmatically in this batch, so the base state is seeded. */
    @BeforeEach
    void seed() {
        park.reset();
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.registerUser(new User("pedro@correo.com", "87654321X"));
        park.setCurrentDate("2027-04-01");
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createIndividualTicket("T009", "pedro@correo.com", "2027-04-20");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.configure(TextFileRepositories.create(DataDirectories.permanent(base)));
        park.persist();
    }

    private String app(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void stateSurvivesRestart() {
        String out = app(SCRIPT);
        assertFalse(out.contains("Error"), out);
        app("EXIT\n");

        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A03").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        for (Attraction<?> attraction : park.getAttractions()) {
            assertTrue(attraction.getOccupants().isEmpty());
        }

        assertEquals(10, park.getPasses().stream().filter(p -> p.getIdentifier().startsWith("SP01:")).count());
        assertTrue(park.findPass("SP01:1").isConsumed());
        assertFalse(park.findPass("SP01:2").isConsumed());
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:2").getPrice());
        assertSame(park.findIndividualTicket("T001"), park.findPass("SP01:2").getTicket());

        Pass special = park.findPass("PE01");
        assertTrue(special instanceof SpecialPass);
        assertTrue(special.isConsumed());
        assertEquals(new BigDecimal("40.00"), special.getPrice());

        assertTrue(park.findPass("SPA:1").isConsumed());
        assertTrue(park.findPass("SPB:1").isConsumed());
        assertSame(park.findIndividualTicket("TG01:12345678Z"), park.findPass("SPA:1").getTicket());

        assertNull(park.findTicket("T009"));
        assertNull(park.findPass("SPF:1"));
    }

    @Test
    void flyModeCoversDelivery3ElementsAndLeavesNoData() throws IOException {
        String out = app(SCRIPT, App.FLY);
        assertTrue(out.contains("ATTRACTION CREATED"));
        assertTrue(out.contains("Error: individual ticket not found: T001"));
        try (Stream<Path> left = Files.list(base.resolve(App.TEMP_DIRECTORY))) {
            assertFalse(left.findAny().isPresent());
        }
        app("EXIT\n");
        assertNull(park.findAttraction("A01"));
        assertTrue(park.getPasses().isEmpty());
        assertEquals(3, park.getTickets().size());
    }
}
