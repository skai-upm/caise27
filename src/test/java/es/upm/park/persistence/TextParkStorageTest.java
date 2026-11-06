package es.upm.park.persistence;

import es.upm.park.business.controller.ParkState;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.GeneralAttraction;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.Ticket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextParkStorageTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private TextParkStorage storage() {
        StoragePaths paths = new StoragePaths(dir);
        PrintStream out = new PrintStream(warnings, true, StandardCharsets.UTF_8);
        return new TextParkStorage(new GroupRepository(paths.groups(), out), new TicketRepository(paths.tickets(), out),
                new AttractionRepository(paths.attractions(), out), new PassRepository(paths.passes(), out));
    }

    @Test
    void missingFilesLoadAsEmptyState() {
        ParkState state = storage().load();
        assertTrue(state.groups().isEmpty());
        assertTrue(state.tickets().isEmpty());
        assertTrue(state.attractions().isEmpty());
    }

    @Test
    void fullRoundTrip() {
        Group group = new Group("G01", "Familia Garcia");
        group.addMember("ana@correo.com");
        group.addMember("pedro@correo.com");
        Group gone = new Group("G09", "Borrado");
        GroupTicket linked = GroupTicket.purchase("TG01", group, DAY, new BigDecimal("40.00"),
                Map.of("ana@correo.com", "12345678Z"));
        GroupTicket detached = GroupTicket.purchase("TG02", gone, DAY, new BigDecimal("45.50"),
                Map.of("pedro@correo.com", "87654321X"));
        detached.detachGroup();
        IndividualTicket single = new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00"));
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        attraction.enter("ana@correo.com");

        storage().save(new ParkState(List.of(group), List.of(linked, detached, single), List.of(attraction)));
        ParkState loaded = storage().load();

        Group loadedGroup = loaded.groups().get(0);
        assertEquals("Familia Garcia", loadedGroup.getName());
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), List.copyOf(loadedGroup.getMemberEmails()));

        GroupTicket loadedLinked = (GroupTicket) loaded.tickets().get(0);
        assertSame(loadedGroup, loadedLinked.getGroup());
        assertEquals("TG01:12345678Z", loadedLinked.getComposition().get(0).getId());
        assertEquals(new BigDecimal("40.00"), loadedLinked.getPrice());

        GroupTicket loadedDetached = (GroupTicket) loaded.tickets().get(1);
        assertNull(loadedDetached.getGroup());
        assertEquals("pedro@correo.com", loadedDetached.getComposition().get(0).getOwnerEmail());
        assertEquals(new BigDecimal("45.50"), loadedDetached.getPrice());
        assertTrue(loadedDetached.getComposition().get(0).isPartOfGroupTicket());

        Ticket loadedSingle = loaded.tickets().get(2);
        assertEquals("ana@correo.com", ((IndividualTicket) loadedSingle).getOwnerEmail());
        assertEquals(DAY, loadedSingle.getDate());

        Attraction<?> loadedAttraction = loaded.attractions().get(0);
        assertEquals("Dragon Rojo", loadedAttraction.getName());
        assertEquals(20, loadedAttraction.getCapacity());
        assertTrue(loadedAttraction.getOccupantEmails().isEmpty());
        assertEquals("", warnings.toString(StandardCharsets.UTF_8));
    }

    @Test
    void passesRoundTripWithTicketsAndConsumedFlag() {
        IndividualTicket single = new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00"));
        GroupTicket group = GroupTicket.purchase("TG01", null, DAY, new BigDecimal("40.00"),
                Map.of("pedro@correo.com", "87654321X"));
        SpeedPass speed = new SpeedPass("SP01:1", single);
        SpecialPass special = new SpecialPass("PE01", group.getComposition().get(0));
        special.consume();
        storage().save(new ParkState(List.of(), List.of(single, group), List.of(), List.of(speed, special)));
        ParkState loaded = storage().load();
        assertEquals(2, loaded.passes().size());
        Pass loadedSpeed = loaded.passes().get(0);
        assertTrue(loadedSpeed instanceof SpeedPass);
        assertEquals(new BigDecimal("8.00"), loadedSpeed.getPrice());
        assertSame(loaded.tickets().get(0), loadedSpeed.getTicket());
        Pass loadedSpecial = loaded.passes().get(1);
        assertTrue(loadedSpecial instanceof SpecialPass);
        assertTrue(loadedSpecial.isConsumed());
        assertSame(((GroupTicket) loaded.tickets().get(1)).getComposition().get(0), loadedSpecial.getTicket());
    }

    @Test
    void attractionTypesRoundTripAndUntypedLinesAreGeneral() throws IOException {
        storage().save(new ParkState(List.of(), List.of(),
                List.of(es.upm.park.business.model.AttractionType.SPECIAL.create("A04", "Pasaje del Terror", 15))));
        assertEquals(es.upm.park.business.model.AttractionType.SPECIAL, storage().load().attractions().get(0).getType());
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\n");
        Attraction<?> legacy = storage().load().attractions().get(0);
        assertEquals(es.upm.park.business.model.AttractionType.GENERAL, legacy.getType());
        assertEquals(20, legacy.getCapacity());
    }

    @Test
    void malformedLinesAreReportedAndSkipped() throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\nbroken line\nA02;Torre;doce\n");
        Files.writeString(dir.resolve("tickets.txt"), "I;T001;2027-04-15;40.00;ana@correo.com\nX;T002\n");
        ParkState state = storage().load();
        assertEquals(1, state.attractions().size());
        assertEquals(1, state.tickets().size());
        String reported = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(reported.contains("broken line"));
        assertTrue(reported.contains("A02;Torre;doce"));
        assertTrue(reported.contains("X;T002"));
    }
}
